package cn.wildfirechat.app.pojo;

public class LoginRequest {
    private String mobile;
    private String code;
    private String clientId;
    private Integer platform;

    /**
     * 设备系统版本
     */
    private String deviceVersion;

    /**
     * 手机型号
     */
    private String deviceModel;

    /**
     * APP版本
     */
    private String appVersion;

    // 微信Id
    private String wechat_unionid;
    // qqid
    private String qq_openid;

    private String displayName;//昵称
    private String portrait;//头像

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCode() {
        return code;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getWechat_unionid() {
        return wechat_unionid;
    }

    public void setWechat_unionid(String wechat_unionid) {
        this.wechat_unionid = wechat_unionid;
    }

    public String getQq_openid() {
        return qq_openid;
    }

    public void setQq_openid(String qq_openid) {
        this.qq_openid = qq_openid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }
}
