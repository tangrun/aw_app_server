package cn.wildfirechat.app.pojo;

import lombok.Data;

@Data
public class VersionRequest {

    long type;

    Integer platform;

    /**
     * 设备系统版本
     */
    String deviceVersion;

    /**
     * 手机型号
     */
    String deviceModel;

    /**
     * APP版本
     */
    String appVersion;
}
