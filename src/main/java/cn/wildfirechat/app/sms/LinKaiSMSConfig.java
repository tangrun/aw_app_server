package cn.wildfirechat.app.sms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix="sms")
@PropertySource(value = "file:config/linkai_sms.properties",encoding = "utf-8")
@Getter
@Setter
public class LinKaiSMSConfig {
    private String smsPath;
    private String smsUser;
    private String smsPwd;

}
