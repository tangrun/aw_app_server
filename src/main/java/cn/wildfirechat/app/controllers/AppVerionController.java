package cn.wildfirechat.app.controllers;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.jpa.*;
import cn.wildfirechat.app.pojo.VersionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public RestResult getVersionInfo(@RequestBody VersionRequest request) {
        Optional<AppVersionEntry> announcement = appVersionRepository.findById(request.getType());
        if (announcement.isPresent()) {
            AppVersionEntry pojo = announcement.get();
            return RestResult.ok(pojo);
        } else {
            return RestResult.error(ERROR_NOT_EXIST);
        }
    }



}
