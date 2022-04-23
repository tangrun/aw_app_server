package cn.wildfirechat.app.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RestController
public class CommonController {

    @Resource
    CommonService commonService;

    @GetMapping("/common/file/{id}")
    public void file(@PathVariable("id") String id, HttpServletResponse response){
        commonService.downloadFile(id,response);
    }

}
