package cn.wildfirechat.app.common;

import com.tencentcloudapi.ape.v20200513.models.DescribeDownloadInfosResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

@OpenAPIDefinition(

)
@RestController
public class CommonController {

    @Resource
    CommonService commonService;

    @GetMapping("/common/file/{id}")
    public void file(@PathVariable("id") String id, HttpServletResponse response){
        commonService.downloadFile(id,response);
    }

}
