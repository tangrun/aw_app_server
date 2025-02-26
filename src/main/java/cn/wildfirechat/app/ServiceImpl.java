package cn.wildfirechat.app;


import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.jpa.*;
import cn.wildfirechat.app.pojo.*;
import cn.wildfirechat.app.shiro.AuthDataSource;
import cn.wildfirechat.app.shiro.PhoneCodeToken;
import cn.wildfirechat.app.shiro.TokenAuthenticationToken;
import cn.wildfirechat.app.shiro.UsernameCodeToken;
import cn.wildfirechat.app.shiro.UsernamePasswordToken;
import cn.wildfirechat.app.sms.SmsService;
import cn.wildfirechat.app.tools.RateLimiter;
import cn.wildfirechat.app.tools.ShortUUIDGenerator;
import cn.wildfirechat.app.tools.Utils;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.*;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.sdk.*;
import cn.wildfirechat.sdk.model.IMResult;
import com.aliyun.oss.*;
import com.aliyun.oss.model.PutObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static cn.wildfirechat.app.RestResult.RestCode.*;
import static cn.wildfirechat.app.jpa.PCSession.PCSessionStatus.*;

@Slf4j
@org.springframework.stereotype.Service
public class ServiceImpl implements Service {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceImpl.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private IMConfig mIMConfig;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserLogRepository userLogRepository;

    @Value("${logs.user_logs_path}")
    private String userLogPath;

    @Autowired
    private ShortUUIDGenerator userNameGenerator;

    @Autowired
    private AuthDataSource authDataSource;


    @Value("${wfc.compat_pc_quick_login}")
    protected boolean compatPcQuickLogin;

    @Value("${media.server.media_type}")
    private int ossType;

    @Value("${media.server_url}")
    private String ossUrl;

    @Value("${media.access_key}")
    private String ossAccessKey;

    @Value("${media.secret_key}")
    private String ossSecretKey;

    @Value("${media.bucket_general_name}")
    private String ossGeneralBucket;
    @Value("${media.bucket_general_domain}")
    private String ossGeneralBucketDomain;

    @Value("${media.bucket_image_name}")
    private String ossImageBucket;
    @Value("${media.bucket_image_domain}")
    private String ossImageBucketDomain;

    @Value("${media.bucket_voice_name}")
    private String ossVoiceBucket;
    @Value("${media.bucket_voice_domain}")
    private String ossVoiceBucketDomain;

    @Value("${media.bucket_video_name}")
    private String ossVideoBucket;
    @Value("${media.bucket_video_domain}")
    private String ossVideoBucketDomain;


    @Value("${media.bucket_file_name}")
    private String ossFileBucket;
    @Value("${media.bucket_file_domain}")
    private String ossFileBucketDomain;

    @Value("${media.bucket_sticker_name}")
    private String ossStickerBucket;
    @Value("${media.bucket_sticker_domain}")
    private String ossStickerBucketDomain;

    @Value("${media.bucket_moments_name}")
    private String ossMomentsBucket;
    @Value("${media.bucket_moments_domain}")
    private String ossMomentsBucketDomain;

    @Value("${media.bucket_favorite_name}")
    private String ossFavoriteBucket;
    @Value("${media.bucket_favorite_domain}")
    private String ossFavoriteBucketDomain;

    @Value("${local.media.temp_storage}")
    private String ossTempPath;

    private ConcurrentHashMap<String, Boolean> supportPCQuickLoginUsers = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        AdminConfig.initAdmin(mIMConfig.admin_url, mIMConfig.admin_secret);
    }


    /**
     * 发送PC端登录消息
     *
     * @param fromUser
     * @param toUser
     * @param platform
     * @param token
     */
    private void sendPcLoginRequestMessage(String fromUser, String toUser, int platform, String token) {
        Conversation conversation = new Conversation();
        conversation.setTarget(toUser);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
        MessagePayload payload = new MessagePayload();
        payload.setType(94);
        if (platform == ProtoConstants.Platform.Platform_WEB) {
            payload.setPushContent("Web端登录请求");
        } else if (platform == ProtoConstants.Platform.Platform_OSX) {
            payload.setPushContent("Mac 端登录请求");
        } else if (platform == ProtoConstants.Platform.Platform_LINUX) {
            payload.setPushContent("Linux 端登录请求");
        } else if (platform == ProtoConstants.Platform.Platform_Windows) {
            payload.setPushContent("Windows 端登录请求");
        } else {
            payload.setPushContent("PC 端登录请求");
        }

        payload.setExpireDuration(60 * 1000);
        payload.setPersistFlag(ProtoConstants.PersistFlag.Not_Persist);
        JsonObject data = new JsonObject();
        data.addProperty("p", platform);
        data.addProperty("t", token);
        payload.setBase64edData(Base64Utils.encodeToString(data.toString().getBytes()));

        try {
            IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(fromUser, conversation, payload);
            if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                LOG.info("send message success");
            } else {
                LOG.error("send message error {}", resultSendMessage != null ? resultSendMessage.getErrorCode().code : "unknown");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("send message error {}", e.getLocalizedMessage());
        }

    }

    @Override
    public RestResult createPcSession(CreateSessionRequest request) {
        String userId = request.getUserId();
        // pc端切换登录用户时，还会带上之前的cookie，通过请求里面是否带有userId来判断是否是切换到新用户
        if (request.getFlag() == 1 && !StringUtils.isEmpty(userId)) {
            Subject subject = SecurityUtils.getSubject();
            userId = (String) subject.getSession().getAttribute("userId");
        }

        if (compatPcQuickLogin) {
            if (userId != null && supportPCQuickLoginUsers.get(userId) == null) {
                userId = null;
            }
        }

        PCSession session = authDataSource.createSession(userId, request.getClientId(), request.getToken(), request.getPlatform());
        if (userId != null) {
            sendPcLoginRequestMessage("admin", userId, request.getPlatform(), session.getToken());
        }
        SessionOutput output = session.toOutput();
        LOG.info("client {} create pc session, key is {}", request.getClientId(), output.getToken());
        return RestResult.ok(output);
    }

    @Override
    public RestResult loginWithSession(String token) {
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        // comment start 如果确定登录不成功，就不通过Shiro尝试登录了
        TokenAuthenticationToken tt = new TokenAuthenticationToken(token);
        PCSession session = authDataSource.getSession(token, false);

        if (session == null) {
            return RestResult.error(ERROR_CODE_EXPIRED);
        } else if (session.getStatus() == Session_Created) {
            return RestResult.error(ERROR_SESSION_NOT_SCANED);
        } else if (session.getStatus() == Session_Scanned) {
            session.setStatus(Session_Pre_Verify);
            authDataSource.saveSession(session);
            LoginResponse response = new LoginResponse();
            try {
                IMResult<InputOutputUserInfo> result = UserAdmin.getUserByUserId(session.getConfirmedUserId());
                if (result.getCode() == 0) {
                    response.setUserName(result.getResult().getDisplayName());
                    response.setPortrait(result.getResult().getPortrait());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return RestResult.result(ERROR_SESSION_NOT_VERIFIED, response);
        } else if (session.getStatus() == Session_Pre_Verify) {
            return RestResult.error(ERROR_SESSION_NOT_VERIFIED);
        } else if (session.getStatus() == Session_Canceled) {
            return RestResult.error(ERROR_SESSION_CANCELED);
        }
        // comment end

        // 执行认证登陆
        // comment start 由于PC端登录之后，可以请求app server创建群公告等。为了保证安全, PC端登录时，也需要在app server创建session。
        try {
            subject.login(tt);
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
        if (subject.isAuthenticated()) {
            LOG.info("Login success");
        } else {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        }
        // comment end

        session = authDataSource.getSession(token, true);
        if (session == null) {
            subject.logout();
            return RestResult.error(RestResult.RestCode.ERROR_CODE_EXPIRED);
        }
        subject.getSession().setAttribute("userId", session.getConfirmedUserId());

        try {
            //使用用户id获取token
            IMResult<OutputGetIMTokenData> tokenResult = UserAdmin.getUserToken(session.getConfirmedUserId(), session.getClientId(), session.getPlatform());
            if (tokenResult.getCode() != 0) {
                LOG.error("Get user failure {}", tokenResult.code);
                subject.logout();
                return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
            }
            //返回用户id，token和是否新建
            LoginResponse response = new LoginResponse();
            response.setUserId(session.getConfirmedUserId());
            response.setToken(tokenResult.getResult().getToken());
            return RestResult.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            subject.logout();
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    @Override
    public RestResult scanPc(String token) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        LOG.info("user {} scan pc, session is {}", userId, token);
        return authDataSource.scanPc(userId, token);
    }

    @Override
    public RestResult confirmPc(ConfirmSessionRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        if (compatPcQuickLogin) {
            if (request.getQuick_login() > 0) {
                supportPCQuickLoginUsers.put(userId, true);
            } else {
                supportPCQuickLoginUsers.remove(userId);
            }
        }

        LOG.info("user {} confirm pc, session is {}", userId, request.getToken());
        return authDataSource.confirmPc(userId, request.getToken());
    }

    @Override
    public RestResult cancelPc(CancelSessionRequest request) {
        return authDataSource.cancelPc(request.getToken());
    }

    @Override
    public RestResult changeName(String newName) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        try {
            IMResult<InputOutputUserInfo> existUser = UserAdmin.getUserByName(newName);
            if (existUser != null) {
                if (existUser.code == ErrorCode.ERROR_CODE_SUCCESS.code) {
                    if (userId.equals(existUser.getResult().getUserId())) {
                        return RestResult.ok(null);
                    } else {
                        return RestResult.error(ERROR_USER_NAME_ALREADY_EXIST);
                    }
                } else if (existUser.code == ErrorCode.ERROR_CODE_NOT_EXIST.code) {
                    existUser = UserAdmin.getUserByUserId(userId);
                    if (existUser == null || existUser.code != ErrorCode.ERROR_CODE_SUCCESS.code || existUser.getResult() == null) {
                        return RestResult.error(ERROR_SERVER_ERROR);
                    }

                    existUser.getResult().setName(newName);
                    IMResult<OutputCreateUser> createUser = UserAdmin.createUser(existUser.getResult());
                    if (createUser.code == ErrorCode.ERROR_CODE_SUCCESS.code) {
                        return RestResult.ok(null);
                    } else {
                        return RestResult.error(ERROR_SERVER_ERROR);
                    }
                } else {
                    return RestResult.error(ERROR_SERVER_ERROR);
                }
            } else {
                return RestResult.error(ERROR_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RestResult.error(ERROR_SERVER_ERROR);
        }
    }

    @Override
    public RestResult complain(String text) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        LOG.error("Complain from user {} where content {}", userId, text);
        adminService.sendTextMessage(userId, "EGEPEP77", text);
        return RestResult.ok(null);
    }

    @Override
    public RestResult getGroupAnnouncement(String groupId) {
        Optional<Announcement> announcement = announcementRepository.findById(groupId);
        if (announcement.isPresent()) {
            GroupAnnouncementPojo pojo = new GroupAnnouncementPojo();
            pojo.groupId = announcement.get().getGroupId();
            pojo.author = announcement.get().getAuthor();
            pojo.text = announcement.get().getAnnouncement();
            pojo.timestamp = announcement.get().getTimestamp();
            return RestResult.ok(pojo);
        } else {
            return RestResult.error(ERROR_GROUP_ANNOUNCEMENT_NOT_EXIST);
        }
    }

    @Override
    public RestResult putGroupAnnouncement(GroupAnnouncementPojo request) {
        if (!StringUtils.isEmpty(request.text)) {
            Assert.isTrue(request.text.length() < 255, "内容不能太长");
            Subject subject = SecurityUtils.getSubject();
            String userId = (String) subject.getSession().getAttribute("userId");
            boolean isGroupMember = false;
            try {
                IMResult<OutputGroupMemberList> imResult = GroupAdmin.getGroupMembers(request.groupId);
                if (imResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS && imResult.getResult() != null && imResult.getResult().getMembers() != null) {
                    for (PojoGroupMember member : imResult.getResult().getMembers()) {
                        if (member.getMember_id().equals(userId)) {
                            if (member.getType() != ProtoConstants.GroupMemberType.GroupMemberType_Removed
                                    && member.getType() != ProtoConstants.GroupMemberType.GroupMemberType_Silent) {
                                isGroupMember = true;
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!isGroupMember) {
                return RestResult.error(ERROR_NO_RIGHT);
            }

            Conversation conversation = new Conversation();
            conversation.setTarget(request.groupId);
            conversation.setType(ProtoConstants.ConversationType.ConversationType_Group);
            MessagePayload payload = new MessagePayload();
            payload.setType(1);
            payload.setSearchableContent("@所有人 " + request.text);
            payload.setMentionedType(2);


            try {
                IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(request.author, conversation, payload);
                if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                    LOG.info("send message success");
                } else {
                    LOG.error("send message error {}", resultSendMessage != null ? resultSendMessage.getErrorCode().code : "unknown");
                    return RestResult.error(ERROR_SERVER_ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("send message error {}", e.getLocalizedMessage());
                return RestResult.error(ERROR_SERVER_ERROR);
            }
        }

        Announcement announcement = new Announcement();
        announcement.setGroupId(request.groupId);
        announcement.setAuthor(request.author);
        announcement.setAnnouncement(request.text);
        request.timestamp = System.currentTimeMillis();
        announcement.setTimestamp(request.timestamp);

        announcementRepository.save(announcement);
        return RestResult.ok(request);
    }

    @Override
    public RestResult saveUserLogs(String userId, MultipartFile file) {
        File localFile = new File(userLogPath, userId + "_" + file.getOriginalFilename());

        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
            return RestResult.error(ERROR_SERVER_ERROR);
        }

        return RestResult.ok(null);
    }

    @Override
    public RestResult addDevice(InputCreateDevice createDevice) {
        try {
            Subject subject = SecurityUtils.getSubject();
            String userId = (String) subject.getSession().getAttribute("userId");

            if (!StringUtils.isEmpty(createDevice.getDeviceId())) {
                IMResult<OutputDevice> outputDeviceIMResult = UserAdmin.getDevice(createDevice.getDeviceId());
                if (outputDeviceIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                    if (!createDevice.getOwners().contains(userId)) {
                        return RestResult.error(ERROR_NO_RIGHT);
                    }
                } else if (outputDeviceIMResult.getErrorCode() != ErrorCode.ERROR_CODE_NOT_EXIST) {
                    return RestResult.error(ERROR_SERVER_ERROR);
                }
            }

            IMResult<OutputCreateDevice> result = UserAdmin.createOrUpdateDevice(createDevice);
            if (result != null && result.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                return RestResult.ok(result.getResult());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(ERROR_SERVER_ERROR);
    }

    @Override
    public RestResult getDeviceList() {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        try {
            IMResult<OutputDeviceList> imResult = UserAdmin.getUserDevices(userId);
            if (imResult != null && imResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                return RestResult.ok(imResult.getResult().getDevices());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(ERROR_SERVER_ERROR);
    }


    @Override
    public RestResult delDevice(InputCreateDevice createDevice) {
        try {
            Subject subject = SecurityUtils.getSubject();
            String userId = (String) subject.getSession().getAttribute("userId");

            if (!StringUtils.isEmpty(createDevice.getDeviceId())) {
                IMResult<OutputDevice> outputDeviceIMResult = UserAdmin.getDevice(createDevice.getDeviceId());
                if (outputDeviceIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                    if (outputDeviceIMResult.getResult().getOwners().contains(userId)) {
                        createDevice.setExtra(outputDeviceIMResult.getResult().getExtra());
                        outputDeviceIMResult.getResult().getOwners().remove(userId);
                        createDevice.setOwners(outputDeviceIMResult.getResult().getOwners());
                        IMResult<OutputCreateDevice> result = UserAdmin.createOrUpdateDevice(createDevice);
                        if (result != null && result.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                            return RestResult.ok(result.getResult());
                        } else {
                            return RestResult.error(ERROR_SERVER_ERROR);
                        }
                    } else {
                        return RestResult.error(ERROR_NO_RIGHT);
                    }
                } else {
                    if (outputDeviceIMResult.getErrorCode() != ErrorCode.ERROR_CODE_NOT_EXIST) {
                        return RestResult.error(ERROR_SERVER_ERROR);
                    } else {
                        return RestResult.error(ERROR_NOT_EXIST);
                    }
                }
            } else {
                return RestResult.error(ERROR_INVALID_PARAMETER);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(ERROR_SERVER_ERROR);
    }

    @Override
    public RestResult sendMessage(SendMessageRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        Conversation conversation = new Conversation();
        conversation.setType(request.type);
        conversation.setTarget(request.target);
        conversation.setLine(request.line);

        MessagePayload payload = new MessagePayload();
        payload.setType(request.content_type);
        payload.setSearchableContent(request.content_searchable);
        payload.setPushContent(request.content_push);
        payload.setPushData(request.content_push_data);
        payload.setContent(request.content);
        payload.setBase64edData(request.content_binary);
        payload.setMediaType(request.content_media_type);
        payload.setRemoteMediaUrl(request.content_remote_url);
        payload.setMentionedType(request.content_mentioned_type);
        payload.setMentionedTarget(request.content_mentioned_targets);
        payload.setExtra(request.content_extra);

        try {
            IMResult<SendMessageResult> imResult = MessageAdmin.sendMessage(userId, conversation, payload);
            if (imResult != null && imResult.getCode() == ErrorCode.ERROR_CODE_SUCCESS.code) {
                return RestResult.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(ERROR_SERVER_ERROR);
    }

    @Override
    public RestResult uploadMedia(int mediaType, MultipartFile file) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        String uuid = new ShortUUIDGenerator().getUserName(userId);
        String fileName = userId + "-" + System.currentTimeMillis() + "-" + uuid + "-" + file.getOriginalFilename();
        File localFile = new File(ossTempPath, fileName);

        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
            return RestResult.error(ERROR_SERVER_ERROR);
        }
        /*
        #Media_Type_GENERAL = 0,
#Media_Type_IMAGE = 1,
#Media_Type_VOICE = 2,
#Media_Type_VIDEO = 3,
#Media_Type_FILE = 4,
#Media_Type_PORTRAIT = 5,
#Media_Type_FAVORITE = 6,
#Media_Type_STICKER = 7,
#Media_Type_MOMENTS = 8
         */
        String bucket;
        String bucketDomain;
        switch (mediaType) {
            case 0:
            default:
                bucket = ossGeneralBucket;
                bucketDomain = ossGeneralBucketDomain;
                break;
            case 1:
                bucket = ossImageBucket;
                bucketDomain = ossImageBucketDomain;
                break;
            case 2:
                bucket = ossVoiceBucket;
                bucketDomain = ossVideoBucketDomain;
                break;
            case 3:
                bucket = ossVideoBucket;
                bucketDomain = ossVideoBucketDomain;
                break;
            case 4:
                bucket = ossFileBucket;
                bucketDomain = ossFileBucketDomain;
                break;
            case 7:
                bucket = ossMomentsBucket;
                bucketDomain = ossMomentsBucketDomain;
                break;
            case 8:
                bucket = ossStickerBucket;
                bucketDomain = ossStickerBucketDomain;
                break;
        }

        String url = bucketDomain + "/" + fileName;
        if (ossType == 1) {
            //构造一个带指定 Region 对象的配置类
            Configuration cfg = new Configuration(Region.region0());
            //...其他参数参考类注释
            UploadManager uploadManager = new UploadManager(cfg);
            //...生成上传凭证，然后准备上传

            //如果是Windows情况下，格式是 D:\\qiniu\\test.png
            String localFilePath = localFile.getAbsolutePath();
            //默认不指定key的情况下，以文件内容的hash值作为文件名
            String key = fileName;
            Auth auth = Auth.create(ossAccessKey, ossSecretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(localFilePath, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
                return RestResult.error(ERROR_SERVER_ERROR);
            }
        } else if (ossType == 2) {
            // 创建OSSClient实例。
            OSS ossClient = new OSSClientBuilder().build(ossUrl, ossAccessKey, ossSecretKey);

            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, localFile);

            // 上传文件。
            try {
                ossClient.putObject(putObjectRequest);
            } catch (OSSException | ClientException e) {
                e.printStackTrace();
                return RestResult.error(ERROR_SERVER_ERROR);
            }
            // 关闭OSSClient。
            ossClient.shutdown();
        } else if (ossType == 3) {
            try {
                // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
//                MinioClient minioClient = new MinioClient("https://play.min.io", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
                MinioClient minioClient = new MinioClient(ossUrl, ossAccessKey, ossSecretKey);

                // 使用putObject上传一个文件到存储桶中。
//                minioClient.putObject("asiatrip",fileName, localFile.getAbsolutePath(), new PutObjectOptions(PutObjectOptions.MAX_OBJECT_SIZE, PutObjectOptions.MIN_MULTIPART_SIZE));
                minioClient.putObject(bucket, fileName, localFile.getAbsolutePath(), new PutObjectOptions(file.getSize(), 0));
            } catch (MinioException e) {
                System.out.println("Error occurred: " + e);
                return RestResult.error(ERROR_SERVER_ERROR);
            } catch (NoSuchAlgorithmException | IOException | InvalidKeyException e) {
                e.printStackTrace();
                return RestResult.error(ERROR_SERVER_ERROR);
            } catch (Exception e) {
                e.printStackTrace();
                return RestResult.error(ERROR_SERVER_ERROR);
            }
        } else if(ossType == 4) {
            //Todo 需要把文件上传到文件服务器。
        } else if(ossType == 5) {
            COSCredentials cred = new BasicCOSCredentials(ossAccessKey, ossSecretKey);
            ClientConfig clientConfig = new ClientConfig();
            String [] ss = ossUrl.split("\\.");
            if(ss.length > 3) {
                if(!ss[1].equals("accelerate")) {
                    clientConfig.setRegion(new com.qcloud.cos.region.Region(ss[1]));
                } else {
                    clientConfig.setRegion(new com.qcloud.cos.region.Region("ap-shanghai"));
                    try {
                        URL u = new URL(ossUrl);
                        clientConfig.setEndPointSuffix(u.getHost());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        return RestResult.error(ERROR_SERVER_ERROR);
                    }
                }
            }

            clientConfig.setHttpProtocol(HttpProtocol.https);
            COSClient cosClient = new COSClient(cred, clientConfig);

            try {
                cosClient.putObject(bucket, fileName, localFile.getAbsoluteFile());
            } catch (CosClientException e) {
                e.printStackTrace();
                return RestResult.error(ERROR_SERVER_ERROR);
            } finally {
                cosClient.shutdown();
            }
        }

        UploadFileResponse response = new UploadFileResponse();
        response.url = url;
        return RestResult.ok(response);
    }

    @Override
    public RestResult putFavoriteItem(FavoriteItem request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        if (!StringUtils.isEmpty(request.url)) {
            try {
                //收藏时需要把对象拷贝到收藏bucket。
                URL mediaURL = new URL(request.url);

                String bucket = null;
                if (mediaURL.getHost().equals(new URL(ossGeneralBucketDomain).getHost())) {
                    bucket = ossGeneralBucket;
                } else if (mediaURL.getHost().equals(new URL(ossImageBucketDomain).getHost())) {
                    bucket = ossImageBucket;
                } else if (mediaURL.getHost().equals(new URL(ossVoiceBucketDomain).getHost())) {
                    bucket = ossVoiceBucket;
                } else if (mediaURL.getHost().equals(new URL(ossVideoBucketDomain).getHost())) {
                    bucket = ossVideoBucket;
                } else if (mediaURL.getHost().equals(new URL(ossFileBucketDomain).getHost())) {
                    bucket = ossFileBucket;
                } else if (mediaURL.getHost().equals(new URL(ossMomentsBucketDomain).getHost())) {
                    bucket = ossMomentsBucket;
                } else if (mediaURL.getHost().equals(new URL(ossStickerBucketDomain).getHost())) {
                    bucket = ossStickerBucket;
                } else if (mediaURL.getHost().equals(new URL(ossFavoriteBucketDomain).getHost())) {
                    //It's already in fav bucket, no need to copy
                    //bucket = ossFavoriteBucket;
                }

                if (bucket != null) {
                    String path = mediaURL.getPath();
                    if (ossType == 1) {
                        Configuration cfg = new Configuration(Region.region0());
                        String fromKey = path.substring(1);
                        Auth auth = Auth.create(ossAccessKey, ossSecretKey);

                        String toBucket = ossFavoriteBucket;
                        String toKey = fromKey;
                        if (!toKey.startsWith(userId)) {
                            toKey = userId + "-" + toKey;
                        }

                        BucketManager bucketManager = new BucketManager(auth, cfg);
                        bucketManager.copy(bucket, fromKey, toBucket, toKey);
                        request.url = ossFavoriteBucketDomain + "/" + fromKey;
                    } else if (ossType == 2) {
                        OSS ossClient = new OSSClient(ossUrl, ossAccessKey, ossSecretKey);
                        path = path.substring(1);
                        String objectName = path;
                        String toKey = path;
                        if (!toKey.startsWith(userId)) {
                            toKey = userId + "-" + toKey;
                        }

                        ossClient.copyObject(bucket, objectName, ossFavoriteBucket, toKey);
                        request.url = ossFavoriteBucketDomain + "/" + toKey;
                        ossClient.shutdown();
                    } else if (ossType == 3) {
                        path = path.substring(bucket.length() + 2);
                        String objectName = path;
                        String toKey = path;
                        if (!toKey.startsWith(userId)) {
                            toKey = userId + "-" + toKey;
                        }
                        MinioClient minioClient = new MinioClient(ossUrl, ossAccessKey, ossSecretKey);
                        minioClient.copyObject(ossFavoriteBucket, toKey, null, null, bucket, objectName, null, null);
                        request.url = ossFavoriteBucketDomain + "/" + toKey;
                    } else if(ossType == 4) {
                        //Todo 需要把收藏的文件保存为永久存储。
                    } else if(ossType == 5) {
                        COSCredentials cred = new BasicCOSCredentials(ossAccessKey, ossSecretKey);
                        ClientConfig clientConfig = new ClientConfig();
                        String [] ss = ossUrl.split("\\.");
                        if(ss.length > 3) {
                            if(!ss[1].equals("accelerate")) {
                                clientConfig.setRegion(new com.qcloud.cos.region.Region(ss[1]));
                            } else {
                                clientConfig.setRegion(new com.qcloud.cos.region.Region("ap-shanghai"));
                                try {
                                    URL u = new URL(ossUrl);
                                    clientConfig.setEndPointSuffix(u.getHost());
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                    return RestResult.error(ERROR_SERVER_ERROR);
                                }
                            }
                        }

                        clientConfig.setHttpProtocol(HttpProtocol.https);
                        COSClient cosClient = new COSClient(cred, clientConfig);

                        path = path.substring(1);
                        String objectName = path;
                        String toKey = path;
                        if (!toKey.startsWith(userId)) {
                            toKey = userId + "-" + toKey;
                        }

                        try {
                            cosClient.copyObject(bucket, objectName, ossFavoriteBucket, toKey);
                            request.url = ossFavoriteBucketDomain + "/" + toKey;
                        } catch (CosClientException e) {
                            e.printStackTrace();
                            return RestResult.error(ERROR_SERVER_ERROR);
                        } finally {
                            cosClient.shutdown();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        request.userId = userId;
        request.timestamp = System.currentTimeMillis();
        favoriteRepository.save(request);
        return RestResult.ok(null);
    }

    @Override
    public RestResult removeFavoriteItems(long id) {
        favoriteRepository.deleteById(id);
        return RestResult.ok(null);
    }

    @Override
    public RestResult getFavoriteItems(long id, int count) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        id = id > 0 ? id : Long.MAX_VALUE;
        List<FavoriteItem> favs = favoriteRepository.loadFav(userId, id, count);
        LoadFavoriteResponse response = new LoadFavoriteResponse();
        response.items = favs;
        response.hasMore = favs.size() == count;
        return RestResult.ok(response);
    }

    @Override
    public RestResult getGroupMembersForPortrait(String groupId) {
        try {
            IMResult<OutputGroupMemberList> groupMemberListIMResult = GroupAdmin.getGroupMembers(groupId);
            if (groupMemberListIMResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
                LOG.error("getGroupMembersForPortrait failure {},{}", groupMemberListIMResult.getErrorCode().getCode(), groupMemberListIMResult.getErrorCode().getMsg());
                return RestResult.error(ERROR_SERVER_ERROR);
            }
            List<PojoGroupMember> groupMembers = new ArrayList<>();
            for (PojoGroupMember member : groupMemberListIMResult.getResult().getMembers()) {
                if (member.getType() != 4)
                    groupMembers.add(member);
            }

            if (groupMembers.size() > 9) {
                groupMembers.sort((o1, o2) -> {
                    if (o1.getType() == 2)
                        return -1;
                    if (o2.getType() == 2)
                        return 1;
                    if (o1.getType() == 1 && o2.getType() != 1)
                        return -1;
                    if (o2.getType() == 1 && o1.getType() != 1)
                        return 1;
                    return Long.compare(o1.getCreateDt(), o2.getCreateDt());
                });
                groupMembers = groupMembers.subList(0, 9);
            }
            List<UserIdPortraitPojo> mids = new ArrayList<>();
            for (PojoGroupMember member : groupMembers) {
                IMResult<InputOutputUserInfo> userInfoIMResult = UserAdmin.getUserByUserId(member.getMember_id());
                if (userInfoIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                    mids.add(new UserIdPortraitPojo(member.getMember_id(), userInfoIMResult.result.getPortrait()));
                } else {
                    mids.add(new UserIdPortraitPojo(member.getMember_id(), ""));
                }
            }
            return RestResult.ok(mids);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("getGroupMembersForPortrait exception", e);
            return RestResult.error(ERROR_SERVER_ERROR);
        }
    }
}
