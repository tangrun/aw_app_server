package cn.wildfirechat.app.common;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.common.entity.UploadFile;
import cn.wildfirechat.app.pojo.UploadFileResponse;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("common")
@RestController
public class CommonController {

    @Resource
    CommonService commonService;

    @PostMapping("file/upload")
    public RestResult<UploadFileResponse> uploadFile(@SessionAttribute String userId,@RequestParam("file") MultipartFile file ){
        Assert.notNull(file, "字段file为空");
        UploadFile uploadFile = commonService.uploadFile(userId, file);
        UploadFileResponse response = new UploadFileResponse();
        response.url = commonService.getDownloadPath(uploadFile);
        return RestResult.ok(response);
    }

    @PostMapping("file/upload2")
    public RestResult<UploadFileResponse> uploadFile2(@SessionAttribute String userId, @RequestParam("file") MultipartFile file ){
        Assert.notNull(file, "字段file为空");
        UploadFile uploadFile = commonService.uploadFile(userId, file);
        UploadFileResponse response = new UploadFileResponse();
        response.url = commonService.getDownloadPath2(uploadFile);
        return RestResult.ok(response);
    }

    @GetMapping("file/{id}")
    public void downloadFile(@PathVariable("id") String id, HttpServletResponse response){
        if (id.contains(".")){
            id = id.substring(0,id.indexOf("."));
        }
        commonService.downloadFile(id,response);
    }

}
