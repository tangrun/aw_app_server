package cn.wildfirechat.app;

import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.jpa.*;
import cn.wildfirechat.app.pojo.LoginResponse;
import cn.wildfirechat.app.pojo.OtherAccountResponse;
import cn.wildfirechat.app.pojo.PhoneCodeLoginRequest;
import cn.wildfirechat.app.shiro.AuthDataSource;
import cn.wildfirechat.app.shiro.UsernameCodeToken;
import cn.wildfirechat.app.shiro.UsernamePasswordToken;
import cn.wildfirechat.app.sms.SmsService;
import cn.wildfirechat.app.tools.RateLimiter;
import cn.wildfirechat.app.tools.ShortUUIDGenerator;
import cn.wildfirechat.app.tools.Utils;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.InputOutputUserInfo;
import cn.wildfirechat.pojos.OutputCreateUser;
import cn.wildfirechat.pojos.OutputGetIMTokenData;
import cn.wildfirechat.pojos.OutputUserStatus;
import cn.wildfirechat.sdk.RelationAdmin;
import cn.wildfirechat.sdk.UserAdmin;
import cn.wildfirechat.sdk.model.IMResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static cn.wildfirechat.app.RestResult.RestCode.*;

@CacheConfig(cacheNames = "user")
@Slf4j
@Service
public class UserService {

    @Autowired
    private AdminService adminService;
    @Autowired
    private FeedBackRepository feedBackRepository;
    @Autowired
    private AuthDataSource authDataSource;
    @Autowired
    private SmsService smsService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserLogRepository userLogRepository;
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private IMConfig mIMConfig;
    @Autowired
    private ShortUUIDGenerator userNameGenerator;

    @Value("${sms.super_code}")
    private String superCode;

    public RestResult destroy(String userId, String code) {
        try {
            UserEntity userEntity = userService.findByUserId(userId);
            Assert.notNull(userEntity, "用户不存在");
            String mobile = userEntity.getMobile();
            if (StringUtils.isNotBlank(mobile)) {
                if (authDataSource.verifyCode(mobile, code) == SUCCESS) {
                    UserAdmin.destroyUser(userId);
                    authDataSource.clearRecode(mobile);
                    userService.deleteUserInfo(mobile);
                    Subject subject = SecurityUtils.getSubject();
                    subject.logout();
                    return RestResult.ok(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
        return RestResult.error(RestResult.RestCode.ERROR_NOT_EXIST);
    }


    /**
     * 发送验证码是 s_ip
     * 重置密码是 p_mobile
     * 密码登录是 lp_mobile
     * 验证码登录是 ls_mobile
     * 第三方登录是 tl_code
     */
    RateLimiter rateLimiter = new RateLimiter(60, 10);

    /**
     * 账号注销
     */
    public RestResult<Void> loginOut(String userId) {
        return userService.setUserState(userId, null, 1, null);
    }


    /**
     * 设置用户状态
     */
    public RestResult<Void> setUserState(String userId, String mobile, @Nonnull Integer state, String remark) {
        UserEntity userEntity = StringUtils.isNotBlank(mobile) ? userService.findByMobile(mobile) : userService.findByUserId(userId);
        if (userEntity == null)
            return RestResult.error(ERROR_USER_NOT_EXIST);
        // 现在注销/封号 没有立即删除 而是在再次验证码登录时执行新建账号操作 才删除旧的
        try {
            if (state == 0) {
                // 正常
                UserAdmin.updateUserBlockStatus(userId, 0);
            } else if (state == 1) {
                // 停用 注销
                UserAdmin.updateUserBlockStatus(userId, 2);
                if (StringUtils.isBlank(remark))
                    remark = "已注销账号";
            } else if (state == 2) {
                // 停用 封号
                UserAdmin.updateUserBlockStatus(userId, 2);
                if (StringUtils.isBlank(remark))
                    remark = "已被系统禁用";
            } else {
                return RestResult.error("不支持的操作");
            }
            userEntity.setState(state);
            userEntity.setLoginRemark(remark);
            userService.saveUserEntity(userEntity);
        } catch (Exception e) {
            log.error("setUserState", e);
            return RestResult.error(e.getMessage());
        }
        return RestResult.ok();
    }

    /**
     * 以手机号为唯一标识 删除旧数据获取最新数据
     * 只是删除app这边的用户信息 如果用户状态正常还是可以在登录的时候新增数据的
     */
    public UserEntity deleteUserInfo(String mobile) {
        UserEntity userEntity = userService.findByMobile(mobile);
        if (userEntity != null) {
            userEntityRepository.delete(userEntity);
        }
        return userEntity;
    }

    @Caching(put = {
            @CachePut(key = "'user:id:'+#userEntity.userId", condition = "#userEntity != null"),
            @CachePut(key = "'user:mobile:'+#userEntity.mobile", condition = "#userEntity != null"),
    })
    public UserEntity saveUserEntity(UserEntity userEntity) {
        userEntityRepository.saveAndFlush(userEntity);
        return userEntity;
    }

    /**
     * 验证码登录
     */
    public RestResult<LoginResponse> loginBySMSCode(HttpServletResponse httpResponse, PhoneCodeLoginRequest request) {
        String mobile = request.getMobile();
        String code = request.getCode();
        String clientId = request.getClientId();
        int platform = request.getPlatform() == null ? 0 : request.getPlatform();

        if (!rateLimiter.isGranted("ls_" + mobile)) {
            return RestResult.error(ERROR_REQUEST_OVER_FREQUENCY);
        }
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        UsernameCodeToken token = new UsernameCodeToken(mobile, code);
        // 执行认证登陆
        try {
            subject.login(token);
        } catch (UnknownAccountException uae) {
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        } catch (IncorrectCredentialsException ice) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (LockedAccountException lae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (ExcessiveAttemptsException eae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (AuthenticationException ae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        }
        if (!subject.isAuthenticated()) {
            token.clear();
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        }

        try {
            /**
             * 是否需要新注册
             */
            boolean register = false;
            UserEntity userEntity = userService.findByMobile(mobile);
            InputOutputUserInfo user = null;
            if (userEntity != null) {
                if (userEntity.getState() != 0) {
                    // 用户异常状态 提示
                    return RestResult.error(userEntity.getLoginRemark());
                }
            } else {
                register = true;
            }

//            boolean coverOld = false;
//            String oldUserId = null;
//            {
//                userEntity = userService.findByMobile(mobile);
//                if (userEntity != null) {
//                    // 已经注册
//                    if (userEntity.getState() == 1) {
//                        // 注销过的再登录就是重新注册
//                        register = true;
//                        coverOld = true;
//                        oldUserId = userEntity.getUserId();
//                        RestResult<Void> result = adminService.destroyUser(userEntity.getUserId());
//                        if (result.getCode() != SUCCESS.code) {
//                            log.info("手机号 {} 重新注册 删除原有用户 {} 失败", mobile, userEntity.getUserId());
//                            return RestResult.error(result.getMessage());
//                        }
//                    } else if (userEntity.getState() != 0) {
//                        // 用户异常状态 提示
//                        return RestResult.error(userEntity.getLoginRemark());
//                    }
//                } else {
//                    // 未注册
//                    register = true;
//                }
//            }
//            log.debug("mobile {} login, register {}, coverOld {},user {}", mobile, register, coverOld, user);


            if (register) {
                //获取用户名。如果用的是shortUUID生成器，是有极小概率会重复的，所以需要去检查是否已经存在相同的userName。
                //ShortUUIDGenerator内的main函数有测试代码，可以观察一下碰撞的概率，这个重复是理论上的，作者测试了几千万次次都没有产生碰撞。
                //另外由于并发的问题，也有同时生成相同的id并同时去检查的并同时通过的情况，但这种情况概率极低，可以忽略不计。
                String userName;
                int tryCount = 0;
                do {
                    tryCount++;
                    userName = userNameGenerator.getUserName(mobile);
                    if (tryCount > 10) {
                        return RestResult.error(ERROR_SERVER_ERROR);
                    }
                } while (!adminService.isUsernameAvailable(userName));

                user = new InputOutputUserInfo();
                user.setName(userName);
                if (request.getDisplayName() != null && !request.getDisplayName().equals("")) {
                    user.setDisplayName(request.getDisplayName());//昵称
                } else {
                    if (mIMConfig.use_random_name) {
                        String displayName = "用户" + (int) (Math.random() * 10000);
                        user.setDisplayName(displayName);
                    } else {
                        user.setDisplayName(mobile);
                    }
                }
                if (request.getPortrait() != null && !request.getPortrait().equals("")) {
                    user.setPortrait(request.getPortrait());//头像
                }
                user.setMobile(mobile);
                IMResult<OutputCreateUser> userIdResult = UserAdmin.createUser(user);
                if (userIdResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                    user.setUserId(userIdResult.getResult().getUserId());
                } else {
                    log.info("Create user failure {}", userIdResult.code);
                    return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
                }
            } else {
                //使用电话号码查询用户信息。
                IMResult<InputOutputUserInfo> userResult = UserAdmin.getUserByMobile(mobile);
                if (userResult.getErrorCode() == ErrorCode.ERROR_CODE_NOT_EXIST) {
                    register = true;
                } else if (userResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
                    log.info("手机号 {} 查询用户信息失败 {}", mobile, userResult.code);
                    return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
                } else {
                    user = userResult.getResult();
                }
            }

            if (user == null) {
                log.info("mobile {} login,but user is null ", mobile);
                return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
            }

            //if (coverOld) {
            //    userService.deleteUserInfo(mobile);
            //}

            //使用用户id获取token
            IMResult<OutputGetIMTokenData> tokenResult = UserAdmin.getUserToken(user.getUserId(), clientId, platform);
            if (tokenResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
                log.error("Get user failure {}", tokenResult.code);
                return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
            }

            subject.getSession().setAttribute("userId", user.getUserId());

            //返回用户id，token和是否新建
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getUserId());
            response.setToken(tokenResult.getResult().getToken());
            response.setRegister(register);

            if (register) {

                // 发送新用户欢迎语
                if (!org.springframework.util.StringUtils.isEmpty(mIMConfig.welcome_for_new_user)) {
                    adminService.sendTextMessage("admin", user.getUserId(), mIMConfig.welcome_for_new_user);
                }

                if (mIMConfig.new_user_robot_friend && !org.springframework.util.StringUtils.isEmpty(mIMConfig.robot_friend_id)) {
                    RelationAdmin.setUserFriend(user.getUserId(), mIMConfig.robot_friend_id, true, null);
                    if (!org.springframework.util.StringUtils.isEmpty(mIMConfig.robot_welcome)) {
                        adminService.sendTextMessage(mIMConfig.robot_friend_id, user.getUserId(), mIMConfig.robot_welcome);
                    }
                }
            } else {
                if (!org.springframework.util.StringUtils.isEmpty(mIMConfig.welcome_for_back_user)) {
                    adminService.sendTextMessage("admin", user.getUserId(), mIMConfig.welcome_for_back_user);
                }
            }

            // 判断是否绑定三方登录信息
            {
                if (userEntity == null) {
                    userEntity = userService.findByUserId(user.getUserId());
                }
                if (userEntity != null) {
                    if (StringUtils.isNotBlank(request.getWechat_unionid())) {
                        userEntity.setWechatUnionid(request.getWechat_unionid().trim());
                        userService.saveUserEntity(userEntity);
                    } else if (StringUtils.isNotBlank(request.getQq_openid())) {
                        userEntity.setQqOpenid(request.getQq_openid().trim());
                        userService.saveUserEntity(userEntity);
                    }
                } else {
                    log.error("user entity null userId: {},mobile: {}", user.getUserId(), user.getMobile());
                }
            }

            // 存储登录记录
            UserLogEntry logEntry = new UserLogEntry();
            logEntry.setUserId(user.getUserId());
            logEntry.setIp(Utils.getIpAddress());
            logEntry.setLoginTime(new Date());
            userLogRepository.save(logEntry);

            Object sessionId = subject.getSession().getId();
            httpResponse.setHeader("authToken", sessionId.toString());
            return RestResult.ok(response);
        } catch (Exception e) {
            log.error("用户登陆失败", e);
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    /**
     * 手机号、密码登录
     *
     * @param httpResponse
     * @param request
     * @return
     */
    public RestResult<LoginResponse> loginByPwd(HttpServletResponse httpResponse, PhoneCodeLoginRequest request) {
        if (!rateLimiter.isGranted("lp_" + request.getMobile())) {
            return RestResult.error(ERROR_REQUEST_OVER_FREQUENCY);
        }
        try {
            //UserEntity userEntity = userService.findByMobile(request.getMobile());
            UserEntity userEntity = userEntityRepository.findFirstByMobile(request.getMobile());
            if (userEntity == null)
                return RestResult.error(ERROR_USER_PASSWORD_ERROR);
            if (userEntity.getState() != 0) {
                return RestResult.error(userEntity.getLoginRemark());
            }

            //判断用户是否存在
//            InputOutputUserInfo user = adminService.getUserByMobile(request.getMobile());
//            if (user == null) {
//                return RestResult.error("账号不存在，请先注册！");
//            }

//            // 判断用户状态
//            IMResult<OutputUserStatus> userStatus = UserAdmin.checkUserBlockStatus(user.getUserId());
//            if (userStatus.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
//                return RestResult.error("获取用户状态失败！");
//            }
//
//            if (userStatus.getResult().getStatus() >= 2) {
//                // 不是正常状态时，提示用户
//                return RestResult.error("账号被禁用或注销！");
//            }

            Subject subject = SecurityUtils.getSubject();
            // 在认证提交前准备 token（令牌）
            cn.wildfirechat.app.shiro.UsernamePasswordToken token = new UsernamePasswordToken(request.getMobile(), request.getCode());
            // 执行认证登陆
            try {
                subject.login(token);
            } catch (UnknownAccountException uae) {
                return RestResult.error(RestResult.RestCode.ERROR_USER_NOT_EXIST);
            } catch (IncorrectCredentialsException ice) {
                return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
            } catch (LockedAccountException lae) {
                //return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
                return RestResult.result(1, "账号被禁用或注销！", null);
            } catch (ExcessiveAttemptsException eae) {
                return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
            } catch (AuthenticationException ae) {
                return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
            }
            if (!subject.isAuthenticated()) {
                token.clear();
                return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
            }

            //使用用户id获取token
            IMResult<OutputGetIMTokenData> tokenResult = UserAdmin.getUserToken(userEntity.getUserId(), request.getClientId(), request.getPlatform());
            if (tokenResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
                log.error("Get user failure {}", tokenResult.code);
                return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
            }

            subject.getSession().setAttribute("userId", userEntity.getUserId());

            //返回用户id，token和是否新建
            LoginResponse response = new LoginResponse();
            response.setUserId(userEntity.getUserId());
            response.setToken(tokenResult.getResult().getToken());
            response.setRegister(false);

            if (!org.apache.commons.lang3.StringUtils.isBlank(mIMConfig.welcome_for_back_user)) {
                adminService.sendTextMessage("admin", userEntity.getUserId(), mIMConfig.welcome_for_back_user);
            }

            // 存储登录记录
            UserLogEntry logEntry = new UserLogEntry();
            logEntry.setUserId(userEntity.getUserId());
            logEntry.setIp(Utils.getIpAddress());
            logEntry.setLoginTime(new Date());
            logEntry.setDeviceModel(request.getDeviceModel());
            logEntry.setDeviceVersion(request.getDeviceVersion());
            logEntry.setAppVersion(request.getAppVersion());
            userLogRepository.save(logEntry);

            Object sessionId = subject.getSession().getId();
            httpResponse.setHeader("authToken", sessionId.toString());
            return RestResult.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception happens {}", e);
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    /**
     * 三方登录
     *
     * @param httpResponse
     * @param request
     * @return
     */
    public RestResult<LoginResponse> loginByOther(HttpServletResponse httpResponse, PhoneCodeLoginRequest request) {
        try {
            UserEntity userEntity = null;
            if (StringUtils.isNotBlank(request.getQq_openid())) {
                //QQ 登录
                //System.out.println("QQ登录：" + request.getQq_openid());
                userEntity = userEntityRepository.findFirstByQqOpenid(request.getQq_openid());
            } else if (StringUtils.isNotBlank(request.getWechat_unionid())) {
                //微信登录
                //System.out.println("微信登录：" + request.getWechat_unionid());
                userEntity = userEntityRepository.findFirstByWechatUnionid(request.getWechat_unionid());
            }

            if (userEntity == null) {
                //System.out.println("没有找到对应的用户信息");
                return RestResult.error(ERROR_USER_NOT_EXIST);
            }
            if (userEntity.getState() != 0) {
                return RestResult.error(userEntity.getLoginRemark());
            }

            if (userEntity.getMobile() == null || userEntity.getMobile().equals("")) {
                // 未绑定手机号
                return RestResult.error(ERROR_USER_NOT_BIND_MOBILE);
            }

            if (!rateLimiter.isGranted("tl_" + userEntity.getMobile())) {
                return RestResult.error(ERROR_REQUEST_OVER_FREQUENCY);
            }

            Subject subject = SecurityUtils.getSubject();
            // 在认证提交前准备 token（令牌）
            UsernameCodeToken token = new UsernameCodeToken(userEntity.getMobile(), superCode);
            //cn.wildfirechat.app.shiro.UsernamePasswordToken token = new UsernamePasswordToken(userEntity.getMobile(), request.getCode());
            // 执行认证登陆
            try {
                subject.login(token);
            } catch (UnknownAccountException uae) {
                return RestResult.error(RestResult.RestCode.ERROR_USER_NOT_EXIST);
            } catch (IncorrectCredentialsException ice) {
                return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
            } catch (LockedAccountException lae) {
                return RestResult.result(1, "账号被禁用或注销！", null);
            } catch (ExcessiveAttemptsException eae) {
                return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
            } catch (AuthenticationException ae) {
                return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
            }
            if (!subject.isAuthenticated()) {
                token.clear();
                return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
            }

            //使用用户id获取token
            IMResult<OutputGetIMTokenData> tokenResult = UserAdmin.getUserToken(userEntity.getUserId(), request.getClientId(), request.getPlatform());
            if (tokenResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
                log.error("Get user failure {}", tokenResult.code);
                return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
            }

            subject.getSession().setAttribute("userId", userEntity.getUserId());

            //返回用户id，token和是否新建
            LoginResponse response = new LoginResponse();
            response.setUserId(userEntity.getUserId());
            response.setToken(tokenResult.getResult().getToken());
            response.setRegister(false);

            if (!org.apache.commons.lang3.StringUtils.isBlank(mIMConfig.welcome_for_back_user)) {
                adminService.sendTextMessage("admin", userEntity.getUserId(), mIMConfig.welcome_for_back_user);
            }

            // 存储登录记录
            UserLogEntry logEntry = new UserLogEntry();
            logEntry.setUserId(userEntity.getUserId());
            logEntry.setIp(Utils.getIpAddress());
            logEntry.setLoginTime(new Date());
            logEntry.setDeviceModel(request.getDeviceModel());
            logEntry.setDeviceVersion(request.getDeviceVersion());
            logEntry.setAppVersion(request.getAppVersion());
            userLogRepository.save(logEntry);

            Object sessionId = subject.getSession().getId();
            httpResponse.setHeader("authToken", sessionId.toString());
            return RestResult.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception happens {}", e);
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    /**
     * 已注册用户 绑定微信、QQ
     *
     * @param userId
     * @param type    1、微信；2、QQ
     * @param otherId
     * @return
     */
    public RestResult<Void> setBindOtherAccount(String userId, int type, String otherId) {
        UserEntity userEntity = userEntityRepository.findFirstByUserId(userId);
        if (userEntity == null)
            return RestResult.error(ERROR_USER_NOT_EXIST);

        if (otherId == null || otherId.equals("")) {
            // 解除账号绑定
            userEntity.setWechatUnionid("");
        } else {
            // 绑定账号
            if (type == 1) {
                // 微信
                UserEntity bind = userEntityRepository.findFirstByWechatUnionid(otherId);
                if (bind != null && !bind.getUserId().equals(userId)) {
                    // 微信号已与其他账号绑定
                    return RestResult.error(ERROR_USER_BIND_OTHERID);
                }
                userEntity.setWechatUnionid(otherId);
            } else if (type == 2) {
                // QQ
                UserEntity bind = userEntityRepository.findFirstByQqOpenid(otherId);
                if (bind != null && !bind.getUserId().equals(userId)) {
                    // QQ号已与其他账号绑定
                    return RestResult.error(ERROR_USER_BIND_OTHERID);
                }
                userEntity.setQqOpenid(otherId);
            } else {
                return RestResult.error(ERROR_INVALID_PARAMETER);
            }
        }
        userService.saveUserEntity(userEntity);
        return RestResult.ok(null);
    }

    /**
     * 已绑定三方账号信息
     *
     * @param userId
     * @return
     */
    public RestResult<OtherAccountResponse> getOtherAccount(String userId) {
        UserEntity userEntity = userEntityRepository.findFirstByUserId(userId);
        if (userEntity == null)
            return RestResult.error(ERROR_USER_NOT_EXIST);
        OtherAccountResponse response = new OtherAccountResponse();
        response.setQqId(userEntity.getQqOpenid() == null ? "" : userEntity.getQqOpenid());
        response.setWxId(userEntity.getWechatUnionid() == null ? "" : userEntity.getWechatUnionid());
        return RestResult.ok(response);
    }

    /**
     * 重置密码
     *
     * @param mobile
     * @param code
     * @param pwd
     * @return
     */
    public RestResult<Void> forgetPassword(String mobile, String code, String pwd) {
        if (!rateLimiter.isGranted("p_" + mobile)) {
            return RestResult.error("请求太频繁，请稍后再试");
        }
        RestResult.RestCode restCode = authDataSource.verifyCode(mobile, code);
        if (restCode != RestResult.RestCode.SUCCESS)
            return RestResult.error(restCode);

        UserEntity userEntity = userService.findByMobile(mobile);
        if (userEntity == null) {
            return RestResult.error(ERROR_USER_NOT_EXIST);
        }

        internalSetNewPassword(userEntity, pwd);
        userService.saveUserEntity(userEntity);
        return RestResult.ok();
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param oldPwd
     * @param pwd
     * @return
     */
    public RestResult<Void> resetPassword(String userId, String oldPwd, String pwd) {
        UserEntity userEntity = userEntityRepository.findFirstByUserId(userId);
        if (userEntity == null) {
            // 没有设置密码时
            userEntity = userService.findByUserId(userId);
            if (userEntity == null) {
                return RestResult.error(ERROR_USER_NOT_EXIST);
            }
        }

        if (StringUtils.isNotBlank(userEntity.getPasswd())
                && !internalVerifyPassword(userEntity, oldPwd)) {
            return RestResult.error(RestResult.RestCode.ERROR_USER_PASSWORD_ERROR);
        } else {
            internalSetNewPassword(userEntity, pwd);
        }
        userEntityRepository.save(userEntity);
        return RestResult.ok(null);
    }

    /**
     * 设置密码
     *
     * @param userId
     * @param pwd
     * @return
     */
    public RestResult<Void> setPassword(String userId, String pwd) {
        UserEntity userEntity = userService.findByUserId(userId);
        if (userEntity == null)
            return RestResult.error(ERROR_USER_NOT_EXIST);
        internalSetNewPassword(userEntity, pwd);
        userService.saveUserEntity(userEntity);
        return RestResult.ok(null);
    }

    private void internalSetNewPassword(UserEntity entity, String password)  {
        String salt = StringUtils.isBlank(entity.getSalt()) ? UUID.randomUUID().toString() : entity.getSalt();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(Sha1Hash.ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("设置密码失败");
        }
        digest.reset();
        digest.update(salt.getBytes(StandardCharsets.UTF_8));
        byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        String hashedPwd = Base64.getEncoder().encodeToString(hashed);
        entity.setPasswd(hashedPwd);
        entity.setSalt(salt);
    }

    private boolean internalVerifyPassword(UserEntity entity, String password) {
        if (StringUtils.isBlank(entity.getSalt())) {
            return StringUtils.equals(DigestUtils.md5DigestAsHex(password.getBytes()), entity.getPasswd());
        } else {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance(Sha1Hash.ALGORITHM_NAME);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new RuntimeException("验证密码失败");
            }
            digest.reset();
            digest.update(entity.getSalt().getBytes(StandardCharsets.UTF_8));
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String hashedPwd = Base64.getEncoder().encodeToString(hashed);
            return hashedPwd.equals(entity.getPasswd());
        }
    }


    /**
     * @param mobile
     * @return null user dont exists
     */
    private UserEntity findOrCreateUserEntityByMobile(String mobile, String userId) {
        //判断用户是否存在
        InputOutputUserInfo user = StringUtils.isNotBlank(mobile) ? adminService.getUserByMobile(mobile)
                : StringUtils.isNotBlank(userId) ? adminService.getUserById(userId)
                : null;
        if (user == null) {
            return null;
        }

        UserEntity userEntity = StringUtils.isNotBlank(mobile) ? userEntityRepository.findFirstByMobile(mobile)
                : StringUtils.isNotBlank(userId) ? userEntityRepository.findFirstByUserId(userId)
                : null;
        if (userEntity == null) {
            userEntity = new UserEntity();
        }
        userEntity.setUserId(user.getUserId());
        userEntity.setMobile(StringUtils.isNotBlank(mobile) ? mobile : user.getMobile());

        try {
            IMResult<OutputUserStatus> outputUserStatusIMResult = UserAdmin.checkUserBlockStatus(userEntity.getUserId());
            if (outputUserStatusIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                if (outputUserStatusIMResult.getResult().getStatus() == 0) {
                    userEntity.setState(0);
                    userEntity.setLoginRemark(null);
                } else if (outputUserStatusIMResult.getResult().getStatus() == 2) {
                    FeedBackEntry feedBackEntry = feedBackRepository.findByUserIdAndType(userEntity.userId, 1);
                    if (feedBackEntry != null) {
                        // 自己注销的
                        userEntity.setState(1);
                        userEntity.setLoginRemark("已申请注销账号");
                    } else {
                        userEntity.setState(2);
                        userEntity.setLoginRemark("已被系统禁用");
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        saveUserEntity(userEntity);
        return userEntity;
    }

    @Cacheable(key = "'user:id:'+#userId", unless = "#result == null ")
    @Nullable
    public UserEntity findByUserId(String userId) {
        return findOrCreateUserEntityByMobile(null, userId);
    }

    @Cacheable(key = "'user:mobile:'+#mobile", unless = "#result == null ")
    @Nullable
    public UserEntity findByMobile(String mobile) {
        return findOrCreateUserEntityByMobile(mobile, null);
    }

    /**
     * @param userId
     * @param type   0登录 1重置 2注销
     * @return
     */
    public RestResult<Void> sendCodeByUserId(String userId, int type) {
        UserEntity userEntity = userService.findByUserId(userId);
        Assert.notNull(userEntity, "用户不存在");
        Assert.isTrue(StringUtils.isNotBlank(userEntity.getMobile()), "未绑定手机号");
        return userService.sendCode(userEntity.getMobile(), type);
    }

    /**
     * @param mobile
     * @param type   0登录 1重置 2注销
     * @return
     */
    public RestResult<Void> sendCode(String mobile, int type) {
        String remoteIp = Utils.getIpAddress();//getIp();
        log.info("request send login sms from {} ip:{} type: {}", mobile, remoteIp, type);

        UserEntity userEntity = userService.findByMobile(mobile);
        if (userEntity != null) {
            if (userEntity.getState() != 0) {
                return RestResult.error(StringUtils.isBlank(userEntity.getLoginRemark()) ? "发送失败" : ("发送失败：" + userEntity.getLoginRemark()));
            }
        }

        //判断当前IP发送是否超频。
        //另外 cn.wildfirechat.app.shiro.AuthDataSource.Count 会对用户发送消息限频
        if (!rateLimiter.isGranted("s_" + remoteIp)) {
            log.info("IP " + remoteIp + " 请求短信超频");
            return RestResult.error(ERROR_SEND_SMS_OVER_FREQUENCY);
        }

        try {
            String code = Utils.getRandomCode(4);
            RestResult<Void> result = authDataSource.insertRecord(mobile, code);
            if (!result.isSuccess()) {
                return result;
            }

            RestResult.RestCode restCode = smsService.sendCode(mobile, code);
            if (restCode == RestResult.RestCode.SUCCESS) {
                return RestResult.ok();
            } else {
                authDataSource.clearRecode(mobile);
                return RestResult.error(restCode);
            }
        } catch (Exception e) {
            // json解析错误
            e.printStackTrace();
            authDataSource.clearRecode(mobile);
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

}
