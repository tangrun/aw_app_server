package cn.wildfirechat.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MediaDownloadPathConfig implements WebMvcConfigurer  {

    @Value("${local.media.temp_storage}")
    private String ossTempPath;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/download/**").addResourceLocations("file:///" +ossTempPath);
    }

}
