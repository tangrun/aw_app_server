package cn.wildfirechat.app;

import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.common.CommonService;
import cn.wildfirechat.app.common.consts.SessionAttributes;
import cn.wildfirechat.app.common.entity.UploadFile;
import cn.wildfirechat.app.jpa.FavoriteItem;
import cn.wildfirechat.app.jpa.UserEntity;
import cn.wildfirechat.app.pojo.*;
import cn.wildfirechat.app.tools.Invoker;
import cn.wildfirechat.app.tools.Utils;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.InputCreateDevice;
import cn.wildfirechat.pojos.InputOutputUserInfo;
import cn.wildfirechat.sdk.UserAdmin;
import cn.wildfirechat.sdk.model.IMResult;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.h2.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
public class AppController {
    private static final Logger LOG = LoggerFactory.getLogger(AppController.class);
    @Autowired
    private Service mService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private UserService userService;

    @GetMapping()
    public Object health() {
        return "Ok";
    }

    @PostMapping(value = "/call/serverInfo")
    public RestResult<CallServerInfoResp> serverInfo() {
        return RestResult.error("不支持，请升级最新版本");
    }

    //region 拓展字段相关设置接口

    /**
     * 设置禁止对方截屏
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/extra/friend/disableScreenshot")
    public RestResult disableScreenshot(@RequestBody ExtraDisableRequest request) {
        Assert.isTrue(!org.apache.commons.lang3.StringUtils.isBlank(request.getTargetId()), "targetId不能为空");
        Boolean disable = Utils.integer2boolean(request.getDisable());
        Assert.notNull(disable, "状态不能为空");

        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        RestResult restResult = adminService.setFriendExtra(request.getTargetId(), userId, new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setNoScreenshot(disable);
            }
        });
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) return restResult;

        restResult = adminService.sendRefreshFriendListNotifyMessage(userId, request.getTargetId());
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) return restResult;


        restResult = adminService.setFriendExtra(userId, request.getTargetId(), new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setDisableScreenshot(disable);
            }
        });
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) return restResult;

        return RestResult.ok(null);
    }

    /**
     * 设置文件禁止转发
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/extra/friend/disableFileForward")
    public RestResult disableFileForward(@RequestBody ExtraDisableRequest request) {
        Assert.isTrue(!org.apache.commons.lang3.StringUtils.isBlank(request.getTargetId()), "targetId不能为空");
        Boolean disable = Utils.integer2boolean(request.getDisable());
        Assert.notNull(disable, "状态不能为空");

        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return adminService.setFriendExtra(userId, request.getTargetId(), new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setDisableFileForward(disable);
            }
        });
    }


    /**
     * 设置离开聊天清空消息记录
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/extra/friend/enableLeaveChatClearList")
    public RestResult enableLeaveChatClearList(@RequestBody ExtraDisableRequest request) {
        Assert.isTrue(!org.apache.commons.lang3.StringUtils.isBlank(request.getTargetId()), "targetId不能为空");
        Boolean disable = Utils.integer2boolean(request.getDisable());
        Assert.notNull(disable, "状态不能为空");

        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return adminService.setFriendExtra(userId, request.getTargetId(), new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setEnableLeaveChatClearList(!disable);
            }
        });
    }

    //endregion

    /*
    移动端登录
     */
    @PostMapping(value = "/send_code", produces = "application/json;charset=UTF-8")
    public Object sendLoginCode(@RequestBody SendCodeRequest request) {
        return userService.sendCode(request.getMobile(),0);
    }

    @PostMapping(value = "/send_reset_code", produces = "application/json;charset=UTF-8")
    public Object sendResetCode(@RequestBody SendCodeRequest request) {
        return userService.sendCode(request.getMobile(),1);
    }

    @PostMapping(value = "/send_destroy_code", produces = "application/json;charset=UTF-8")
    public Object sendDestroyCode(@SessionAttribute(SessionAttributes.userId) String userId) {
        return userService.sendCodeByUserId(userId, 2);
    }

    @PostMapping(value = "/destroy", produces = "application/json;charset=UTF-8")
    public Object destroy(@SessionAttribute(SessionAttributes.userId) String userId,@RequestBody DestroyRequest code ) {
        return userService.destroy(userId, code.getCode());
    }

    /**
     * 短信登录
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/login", produces = "application/json;charset=UTF-8")
    public Object loginBySMS(@RequestBody PhoneCodeLoginRequest request, HttpServletResponse response) {
        request.setPlatform(request.getPlatform() == null ? 0 : request.getPlatform());
        return userService.loginBySMSCode(response, request);
    }

    /**
     * 密码登录
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/api/login", produces = "application/json;charset=UTF-8")
    public Object loginByPwd(@RequestBody PhoneCodeLoginRequest request, HttpServletResponse response) {
        request.setPlatform(request.getPlatform() == null ? 0 : request.getPlatform());
        return userService.loginByPwd(response, request);
    }

    /**
     * 三方登录
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/api/otherLogin", produces = "application/json;charset=UTF-8")
    public Object loginOther(@RequestBody PhoneCodeLoginRequest request, HttpServletResponse response) {
        request.setPlatform(request.getPlatform() == null ? 0 : request.getPlatform());
        return userService.loginByOther(response, request);
    }

    /**
     * 绑定三方账号
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/bindOtherAccount", produces = "application/json;charset=UTF-8")
    public RestResult<Void> setBindOtherAccount(@SessionAttribute(SessionAttributes.userId) String userId, @RequestBody PhoneCodeLoginRequest request) {
        return userService.setBindOtherAccount(userId, request.getPlatform(),request.getCode());
    }

    /**
     * 绑定三方账号
     *
     * @return
     */
    @PostMapping(value = "/api/otherAccount", produces = "application/json;charset=UTF-8")
    public RestResult<OtherAccountResponse> getBindOtherAccount(@SessionAttribute(SessionAttributes.userId) String userId) {
        return userService.getOtherAccount(userId);
    }

    /**
     * 设置密码
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/setpwd", produces = "application/json;charset=UTF-8")
    public RestResult<Void> setPassword(@SessionAttribute(SessionAttributes.userId) String userId, @Validated(ChangePasswordRequest.Set.class) @RequestBody ChangePasswordRequest request) {
        return userService.setPassword(userId, request.getNewPwd());
    }

    /**
     * 修改密码
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/changePwd", produces = "application/json;charset=UTF-8")
    public RestResult<Void> changePassword(@SessionAttribute(SessionAttributes.userId) String userId, @Validated(ChangePasswordRequest.Reset.class) @RequestBody ChangePasswordRequest request) {
        return userService.resetPassword(userId, request.getOldPwd(), request.getNewPwd());
    }

    /**
     * 忘记密码
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/forgetPassword", produces = "application/json;charset=UTF-8")
    public RestResult<Void> setForgetPassword(@Validated(ChangePasswordRequest.Forget.class) @RequestBody ChangePasswordRequest request) {
        return userService.forgetPassword(request.getMobile(), request.getCode(), request.getNewPwd());
    }

    /*
    PC扫码操作
    1, PC -> App     创建会话
    2, PC -> App     轮询调用session_login进行登陆，如果已经扫码确认返回token，否则返回错误码9（已经扫码还没确认)或者10(还没有被扫码)
     */
    @CrossOrigin
    @PostMapping(value = "/pc_session", produces = "application/json;charset=UTF-8")
    public Object createPcSession(@RequestBody CreateSessionRequest request) {
        return mService.createPcSession(request);
    }

    @CrossOrigin
    @PostMapping(value = "/session_login/{token}", produces = "application/json;charset=UTF-8")
    public Object loginWithSession(@PathVariable("token") String token) {
        LOG.info("receive login with session key {}", token);
        RestResult timeoutResult = RestResult.error(RestResult.RestCode.ERROR_SESSION_EXPIRED);
        ResponseEntity<RestResult> timeoutResponseEntity = new ResponseEntity<>(timeoutResult, HttpStatus.OK);
        int timeoutSecond = 60;
        DeferredResult<ResponseEntity> deferredResult = new DeferredResult<>(timeoutSecond * 1000L, timeoutResponseEntity);
        CompletableFuture.runAsync(() -> {
            try {
                int i = 0;
                while (i < timeoutSecond) {
                    RestResult restResult = mService.loginWithSession(token);
                    if (restResult.getCode() == RestResult.RestCode.ERROR_SESSION_NOT_VERIFIED.code && restResult.getResult() != null) {
                        deferredResult.setResult(new ResponseEntity(restResult, HttpStatus.OK));
                        break;
                    } else if (restResult.getCode() == RestResult.RestCode.SUCCESS.code

                            || restResult.getCode() == RestResult.RestCode.ERROR_SESSION_EXPIRED.code
                            || restResult.getCode() == RestResult.RestCode.ERROR_SERVER_ERROR.code
                            || restResult.getCode() == RestResult.RestCode.ERROR_SESSION_CANCELED.code
                            || restResult.getCode() == RestResult.RestCode.ERROR_CODE_INCORRECT.code) {
                        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
                        if (restResult.getCode() == RestResult.RestCode.SUCCESS.code) {
                            Subject subject = SecurityUtils.getSubject();
                            Object sessionId = subject.getSession().getId();
                            builder.header("authToken", sessionId.toString());
                        }
                        deferredResult.setResult(builder.body(restResult));
                        break;
                    } else {
                        TimeUnit.SECONDS.sleep(1);
                    }
                    i++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                deferredResult.setResult(new ResponseEntity(RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR), HttpStatus.OK));
            }
        }, Executors.newCachedThreadPool());
        return deferredResult;
    }

    /* 手机扫码操作
    1，扫码，调用/scan_pc接口。
    2，调用/confirm_pc 接口进行确认
     */
    @PostMapping(value = "/scan_pc/{token}", produces = "application/json;charset=UTF-8")
    public Object scanPc(@PathVariable("token") String token) {
        return mService.scanPc(token);
    }

    @PostMapping(value = "/confirm_pc", produces = "application/json;charset=UTF-8")
    public Object confirmPc(@RequestBody ConfirmSessionRequest request) {
        return mService.confirmPc(request);
    }
    @PostMapping(value = "/cancel_pc", produces = "application/json;charset=UTF-8")
    public Object cancelPc(@RequestBody CancelSessionRequest request) {
        return mService.cancelPc(request);
    }

    /*
    修改野火账户
    */
    @CrossOrigin
    @PostMapping(value = "/change_name", produces = "application/json;charset=UTF-8")
    public Object changeName(@RequestBody ChangeNameRequest request) {
        if (StringUtils.isNullOrEmpty(request.getNewName())) {
            return RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER);
        }
        return mService.changeName(request.getNewName());
    }


    /*
    群公告相关接口
     */
    @CrossOrigin
    @PostMapping(value = "/put_group_announcement", produces = "application/json;charset=UTF-8")
    public Object putGroupAnnouncement(@RequestBody GroupAnnouncementPojo request) {
        return mService.putGroupAnnouncement(request);
    }

    @CrossOrigin
    @PostMapping(value = "/get_group_announcement", produces = "application/json;charset=UTF-8")
    public Object getGroupAnnouncement(@RequestBody GroupIdPojo request) {
        return mService.getGroupAnnouncement(request.groupId);
    }

    /*
    客户端上传协议栈日志
     */
    @PostMapping(value = "/logs/{userId}/upload")
    public Object uploadFiles(@RequestParam("file") MultipartFile file, @PathVariable("userId") String userId) throws IOException {
        return mService.saveUserLogs(userId, file);
    }

    /*
    投诉和建议
    */
    @CrossOrigin
    @PostMapping(value = "/complain", produces = "application/json;charset=UTF-8")
    public Object complain(@RequestBody ComplainRequest request) {
        return mService.complain(request.text);
    }

    /*
    物联网相关接口
     */
    @PostMapping(value = "/things/add_device")
    public Object addDevice(@RequestBody InputCreateDevice createDevice) {
        return mService.addDevice(createDevice);
    }

    @PostMapping(value = "/things/list_device")
    public Object getDeviceList() {
        return mService.getDeviceList();
    }

    @PostMapping(value = "/things/del_device")
    public Object delDevice(@RequestBody InputCreateDevice createDevice) {
        return mService.delDevice(createDevice);
    }

    /*
    发送消息
     */
    @PostMapping(value = "/messages/send")
    public Object sendMessage(@RequestBody SendMessageRequest sendMessageRequest) {
        return mService.sendMessage(sendMessageRequest);
    }

    @PostMapping(value = "/messages/delete")
    public Object deleteMessage(@RequestBody InputMessageUidRequest request) {
        Assert.notNull(request.getMessageUid(), "消息ID不能为空");
        return adminService.deleteMessage(request.getMessageUid());
    }

    @PostMapping(value = "/messages/screenshot_report")
    public Object screenshotReport(@RequestBody InputTargetIdRequest request) throws Exception {
        Assert.notNull(request.getTargetId(), "targetId不能为空");
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");


        IMResult<InputOutputUserInfo> result = UserAdmin.getUserByUserId(userId);
        if (result != null) {
            if (result.code == ErrorCode.ERROR_CODE_SUCCESS.code) {
                if (result.result == null) {
                    return RestResult.error(RestResult.RestCode.ERROR_NOT_EXIST);
                } else {
                    return adminService.sendTipNotificationMessage(userId, request.getTargetId(), result.result.getDisplayName() + "将聊天内容截屏了");
                }
            } else {
                return RestResult.result(result);
            }
        } else
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);

    }

    /*
    iOS设备Share extension分享图片文件等使用
     */
    @PostMapping(value = "/media/upload/{media_type}")
    public Object uploadMedia(@RequestParam("file") MultipartFile file, @PathVariable("media_type") int mediaType) throws IOException {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        UploadFile uploadFile = commonService.uploadFile(userId, file);
        UploadFileResponse response = new UploadFileResponse();
        response.url = commonService.getDownloadPath(uploadFile);
        return RestResult.ok(response);
    }

    @CrossOrigin
    @PostMapping(value = "/fav/add", produces = "application/json;charset=UTF-8")
    public Object putFavoriteItem(@RequestBody FavoriteItem request) {
        return mService.putFavoriteItem(request);
    }

    @CrossOrigin
    @PostMapping(value = "/fav/del/{fav_id}", produces = "application/json;charset=UTF-8")
    public Object removeFavoriteItem(@PathVariable("fav_id") int favId) {
        return mService.removeFavoriteItems(favId);
    }

    @CrossOrigin
    @PostMapping(value = "/fav/list", produces = "application/json;charset=UTF-8")
    public Object getFavoriteItems(@RequestBody LoadFavoriteRequest request) {
        return mService.getFavoriteItems(request.id, request.count);
    }

    @CrossOrigin
    @PostMapping(value = "/group/members_for_portrait", produces = "application/json;charset=UTF-8")
    public Object getGroupMembersForPortrait(@RequestBody GroupIdPojo groupIdPojo) {
        return mService.getGroupMembersForPortrait(groupIdPojo.groupId);
    }
}
