package cn.wildfirechat.app.shiro;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.jpa.*;
import cn.wildfirechat.app.pojo.SessionOutput;
import cn.wildfirechat.app.tools.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

import static cn.wildfirechat.app.RestResult.RestCode.*;
import static cn.wildfirechat.app.jpa.PCSession.PCSessionStatus.*;

@Service
public class AuthDataSource {
    private static final Logger LOG = LoggerFactory.getLogger(AuthDataSource.class);
    @Value("${sms.super_code}")
    private String superCode;

    @Autowired
    private PCSessionRepository pcSessionRepository;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private UserEntityRepository userRepository;

    public RestResult<Void> insertRecord(String mobile, String code) {
        if (!Utils.isMobile(mobile)) {
            LOG.error("Not valid mobile {}", mobile);
            return RestResult.error(RestResult.RestCode.ERROR_INVALID_MOBILE);
        }

        Optional<Record> optional = recordRepository.findById(mobile);
        Record record;
        if (optional.isPresent()){
            record = optional.get();
            if (System.currentTimeMillis() - record.getTimestamp() < 15 * 60 * 1000) {
                LOG.error("Send code over frequency. timestamp {}, now {}", record.getTimestamp(), System.currentTimeMillis());
                RestResult<Void> result = RestResult.error(ERROR_SEND_SMS_OVER_FREQUENCY);
                return result.setMessage("验证码15分钟内有效，请勿重复获取");
            }
        }
        record = new Record(code,mobile );
        if (!record.increaseAndCheck()) {
            LOG.error("Count check failure, already send {} messages today", record.getRequestCount());
            RestResult.RestCode c = RestResult.RestCode.ERROR_SEND_SMS_OVER_FREQUENCY;
            c.msg = "发送给用户 " + mobile + " 超出频率限制";
            return RestResult.error(RestResult.RestCode.ERROR_SEND_SMS_OVER_FREQUENCY);
        }

        record.setCode(code);
        record.setTimestamp(System.currentTimeMillis());
        recordRepository.save(record);
        return RestResult.ok();
    }

    public void clearRecode(String mobile) {
        try {
            recordRepository.deleteById(mobile);
        } catch (Exception e) {
        }
    }

    public RestResult.RestCode verifyPassword(String mobile, String password) {
        //判断用户是否已经设置密码
        UserEntity userEntity = userRepository.findFirstByMobile(mobile);
        if(userEntity != null && userEntity.passwd.equals(DigestUtils.md5DigestAsHex(password.getBytes()))){
            // 密码不正确时
            return RestResult.RestCode.SUCCESS;
        }
        return RestResult.RestCode.ERROR_USER_PASSWORD_ERROR;
    }

    public RestResult.RestCode verifyCode(String mobile, String code) {
        if (StringUtils.isEmpty(superCode) || !code.equals(superCode)) {
            Optional<Record> recordOptional = recordRepository.findById(mobile);
            if (!recordOptional.isPresent()) {
                LOG.error("code not exist");
                return RestResult.RestCode.ERROR_CODE_INCORRECT;
            }
            if(!recordOptional.get().getCode().equals(code)) {
                LOG.error("code not matched");
                return RestResult.RestCode.ERROR_CODE_INCORRECT;
            }

            if (System.currentTimeMillis() - recordOptional.get().getTimestamp() > 5 * 60 * 1000) {
                LOG.error("Code expired. timestamp {}, now {}", recordOptional.get().getTimestamp(), System.currentTimeMillis());
                return RestResult.RestCode.ERROR_CODE_EXPIRED;
            }
        }
        return RestResult.RestCode.SUCCESS;
    }

    public PCSession createSession(String userId, String clientId, String token, int platform) {
        PCSession session = new PCSession();
        session.setConfirmedUserId(userId);
        session.setStatus(StringUtils.isEmpty(userId) ? Session_Created : Session_Scanned);
        session.setClientId(clientId);
        session.setCreateDt(System.currentTimeMillis());
        session.setPlatform(platform);
        session.setDuration(300 * 1000); //300 seconds

        if (StringUtils.isEmpty(token)) {
            token = UUID.randomUUID().toString();
        }

        session.setToken(token);

        pcSessionRepository.save(session);
        return session;
    }

    public PCSession getSession(String token, boolean clear) {
        Optional<PCSession> session = pcSessionRepository.findById(token);
        if (clear) {
            pcSessionRepository.deleteById(token);
        }
        return session.orElse(null);
    }

    public void saveSession(PCSession session) {
        pcSessionRepository.save(session);
    }

    public RestResult scanPc(String userId, String token) {
        Optional<PCSession> session = pcSessionRepository.findById(token);
        if (session.isPresent()) {
            SessionOutput output = session.get().toOutput();
            LOG.info("user {} scan pc, session {} expired time left {}", userId, token, output.getExpired());
            if (output.getExpired() > 0) {
                session.get().setStatus(Session_Scanned);
                session.get().setConfirmedUserId(userId);
                output.setStatus(Session_Scanned);
                output.setUserId(userId);
                pcSessionRepository.save(session.get());
                return RestResult.ok(output);
            } else {
                return RestResult.error(RestResult.RestCode.ERROR_SESSION_EXPIRED);
            }
        } else {
            LOG.info("user {} scan pc, session {} not exist!", userId, token);
            return RestResult.error(RestResult.RestCode.ERROR_SESSION_EXPIRED);
        }
    }

    public RestResult confirmPc(String userId, String token) {
        Optional<PCSession> session = pcSessionRepository.findById(token);
        if (session.isPresent()) {
            SessionOutput output = session.get().toOutput();
            LOG.info("user {} confirm pc, session {} expired time left {}", userId, token, output.getExpired());
            if (output.getExpired() > 0) {
                session.get().setStatus(Session_Verified);
                output.setStatus(Session_Verified);
                session.get().setConfirmedUserId(userId);
                pcSessionRepository.save(session.get());
                return RestResult.ok(output);
            } else {
                return RestResult.error(RestResult.RestCode.ERROR_SESSION_EXPIRED);
            }
        } else {
            LOG.error("user {} scan pc, session {} not exist!", userId, token);
            return RestResult.error(RestResult.RestCode.ERROR_SESSION_EXPIRED);
        }
    }

    public RestResult cancelPc(String token) {
        LOG.error("session {} canceled", token);
        Optional<PCSession> session = pcSessionRepository.findById(token);
        if (session.isPresent()) {
            session.get().setStatus(Session_Canceled);
            pcSessionRepository.save(session.get());
        }

        return RestResult.ok(null);
    }

    public RestResult.RestCode checkPcSession(String token) {
        Optional<PCSession> session = pcSessionRepository.findById(token);
        if (session.isPresent()) {
            if (session.get().getStatus() == Session_Verified) {
                //使用用户id获取token
                return SUCCESS;
            } else {
                if (session.get().getStatus() == Session_Created) {
                    return ERROR_SESSION_NOT_SCANED;
                } else if (session.get().getStatus() == Session_Canceled) {
                    return ERROR_SESSION_CANCELED;
                } else {
                    return ERROR_SESSION_NOT_VERIFIED;
                }
            }
        } else {
            return RestResult.RestCode.ERROR_SESSION_EXPIRED;
        }
    }

    public String getUserId(String token, boolean clear) {
        Optional<PCSession> session = pcSessionRepository.findById(token);
        if (clear) {
            pcSessionRepository.deleteById(token);
        }

        if (session.isPresent()) {
            return session.get().getConfirmedUserId();
        }

        return null;
    }
}
