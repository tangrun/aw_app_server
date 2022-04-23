package cn.wildfirechat.app;

import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.sdk.model.IMResult;

public class RestResult<T> {
    public enum RestCode {
        SUCCESS(0, "success"),
        ERROR_INVALID_MOBILE(1, "无效的电话号码"),
        ERROR_SEND_SMS_OVER_FREQUENCY(3, "请求验证码太频繁"),
        ERROR_SERVER_ERROR(4, "服务器异常"),
        ERROR_CODE_EXPIRED(5, "验证码已过期"),
        ERROR_CODE_INCORRECT(6, "验证码错误"),
        ERROR_SERVER_CONFIG_ERROR(7, "服务器配置错误"),
        ERROR_SESSION_EXPIRED(8, "会话不存在或已过期"),
        ERROR_SESSION_NOT_VERIFIED(9, "会话没有验证"),
        ERROR_SESSION_NOT_SCANED(10, "会话没有被扫码"),
        ERROR_SERVER_NOT_IMPLEMENT(11, "功能没有实现"),
        ERROR_GROUP_ANNOUNCEMENT_NOT_EXIST(12, "群公告不存在"),
        ERROR_NOT_LOGIN(13, "没有登录"),
        ERROR_NO_RIGHT(14, "没有权限"),
        ERROR_INVALID_PARAMETER(15, "无效参数"),
        ERROR_NOT_EXIST(16, "对象不存在"),
        ERROR_USER_NAME_ALREADY_EXIST(17, "用户名已经存在"),
        ERROR_SESSION_CANCELED(18, "会话已经取消"),
        ERROR_FILE_DOWNLOAD_ERROR(19, "文件下载失败"),
        ERROR_USER_NOT_EXIST(20, "用户不存在"),
        ERROR_USER_PASSWORD_ERROR(21, "密码错误"),
        ERROR_REQUEST_ERROR(22, "请求出错"),
        ERROR_CREATE_ERROR(23, "创建失败"),
        ;
        public int code;
        public String msg;

        RestCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }

    private int code;
    private String message;
    private T result;

    public static <T>  RestResult<T> ok(T object) {
        return new RestResult<T>(RestCode.SUCCESS, object);
    }

    public static <T>  RestResult<T> error(RestCode code) {
        return new RestResult<T>(code, null);
    }

    public static <T> RestResult<T> result(RestCode code, T object) {
        return new RestResult<T>(code, object);
    }

    public static <T> RestResult<T> result(int code, String message, T object) {
        RestResult<T> r = new RestResult<T>(RestCode.SUCCESS, object);
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> RestResult<T> result(IMResult<?> imResult) {
        return result(imResult, null);
    }

    public static <T> RestResult<T> result(IMResult<?> imResult, String msg) {
        ErrorCode errorCode = imResult.getErrorCode();
        return result(errorCode.code, msg == null ? errorCode.msg : null, null);
    }

    private RestResult(RestCode code, T result) {
        this.code = code.code;
        this.message = code.msg;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public RestResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getResult() {
        return result;
    }

    public RestResult<T> setResult(T result) {
        this.result = result;
        return this;
    }
}
