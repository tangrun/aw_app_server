package cn.wildfirechat.app.controllers;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.jpa.AppVersionEntry;
import cn.wildfirechat.app.jpa.AppVersionRepository;
import cn.wildfirechat.app.jpa.UserLogEntry;
import cn.wildfirechat.app.jpa.UserLogRepository;
import cn.wildfirechat.app.pojo.VersionRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

import static cn.wildfirechat.app.RestResult.RestCode.ERROR_NOT_EXIST;

@Slf4j
@RestController
@RequestMapping("appVersion")
public class AppVerionController {

    @Autowired
    private AppVersionRepository appVersionRepository;

    @Autowired
    private UserLogRepository userLogRepository;

    /**
     * 意见反馈
     *
     * @param request
     * @return
     */
    @PostMapping(value = "GetVersionInfo", produces = "application/json;charset=UTF-8")
    public RestResult<AppVersionEntry> getVersionInfo(@RequestBody VersionRequest request) {
        if(request.getAppVersion() != null && !request.getAppVersion().equals("")){
            Subject subject = SecurityUtils.getSubject();
            String userId = (String) subject.getSession().getAttribute("userId");
            UserLogEntry logEntry = new UserLogEntry();
            logEntry.setUserId(userId);
            logEntry.setIp(getIp());
            logEntry.setLoginTime(new Date());
            logEntry.setDeviceModel(request.getDeviceModel());
            logEntry.setDeviceVersion(request.getDeviceVersion());
            logEntry.setAppVersion(request.getAppVersion());
            userLogRepository.save(logEntry);
        }

        AppVersionEntry announcement = appVersionRepository.findById(request.getType());
        if (announcement!=null) {
            return RestResult.ok(announcement);
        } else {
            return RestResult.error(ERROR_NOT_EXIST);
        }
    }


    /**
     * 获取IP地址
     * @return
     */
    private String getIp() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String headerName = "x-forwarded-for";
        String ip = request.getHeader(headerName);
        if (null != ip && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个IP才是真实IP,它们按照英文逗号','分割
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (checkIp(ip)) {
            headerName = "Proxy-Client-IP";
            ip = request.getHeader(headerName);
        }
        if (checkIp(ip)) {
            headerName = "WL-Proxy-Client-IP";
            ip = request.getHeader(headerName);
        }
        if (checkIp(ip)) {
            headerName = "HTTP_CLIENT_IP";
            ip = request.getHeader(headerName);
        }
        if (checkIp(ip)) {
            headerName = "HTTP_X_FORWARDED_FOR";
            ip = request.getHeader(headerName);
        }
        if (checkIp(ip)) {
            headerName = "X-Real-IP";
            ip = request.getHeader(headerName);
        }
        if (checkIp(ip)) {
            return request.getRemoteAddr();
        }
        return ip;
    }

    private boolean checkIp(String ip) {
        if (null == ip || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            return true;
        }
        return false;
    }

}
