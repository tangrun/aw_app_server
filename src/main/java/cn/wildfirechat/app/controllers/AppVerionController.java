package cn.wildfirechat.app.controllers;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.jpa.AppVersionEntry;
import cn.wildfirechat.app.jpa.AppVersionRepository;
import cn.wildfirechat.app.pojo.VersionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static cn.wildfirechat.app.RestResult.RestCode.ERROR_NOT_EXIST;

@Slf4j
@RestController
@RequestMapping("appVersion")
public class AppVerionController {


    @Autowired
    private AppVersionRepository appVersionRepository;

    /**
     * 意见反馈
     *
     * @param request
     * @return
     */
    @PostMapping(value = "GetVersionInfo", produces = "application/json;charset=UTF-8")
    public RestResult<AppVersionEntry> getVersionInfo(@RequestBody VersionRequest request) {
        AppVersionEntry announcement = appVersionRepository.findById(request.getType());
        if (announcement!=null) {
            return RestResult.ok(announcement);
        } else {
            return RestResult.error(ERROR_NOT_EXIST);
        }
    }



}
