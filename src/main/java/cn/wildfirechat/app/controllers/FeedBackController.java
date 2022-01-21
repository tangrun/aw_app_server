package cn.wildfirechat.app.controllers;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.jpa.FeedBackEntry;
import cn.wildfirechat.app.jpa.FeedBackRepository;
import cn.wildfirechat.app.tools.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("feedback")
public class FeedBackController {

    @Value("${local.media.temp_storage}")
    private String ossTempPath;

    @Value("${media.bucket_general_domain}")
    private String imgUrl;

    @Autowired
    private FeedBackRepository feedBackRepository;

    /**
     * 意见反馈
     *
     * @param request
     * @return
     */
    @PostMapping(value = "AddFeedback", produces = "application/json;charset=UTF-8")
    public RestResult addFeedback(@RequestBody FeedBackEntry request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        request.user_id = userId;
        request.create_time = new Date();

        // 图片处理
        if(request.attach_file != null && request.attach_file.length() > 60){
            String fileName = "feedback/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.attach_file,ossTempPath + fileName)){
                request.attach_file = imgUrl + fileName;
            }
        }
        if(request.attach_file1 != null && request.attach_file1.length() > 60){
            String fileName = "feedback/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.attach_file1,ossTempPath + fileName)){
                request.attach_file1 = imgUrl + fileName;
            }
        }
        if(request.attach_file2 != null && request.attach_file2.length() > 60){
            String fileName = "feedback/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.attach_file2,ossTempPath + fileName)){
                request.attach_file2 = imgUrl + fileName;
            }
        }
        if(request.attach_file3 != null && request.attach_file3.length() > 60){
            String fileName = "feedback/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.attach_file3,ossTempPath + fileName)){
                request.attach_file3 = imgUrl + fileName;
            }
        }
        if(request.attach_file4 != null && request.attach_file4.length() > 60){
            String fileName = "feedback/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.attach_file4,ossTempPath + fileName)){
                request.attach_file4 = imgUrl + fileName;
            }
        }
        if(request.attach_file5 != null && request.attach_file5.length() > 60){
            String fileName = "feedback/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.attach_file5,ossTempPath + fileName)){
                request.attach_file5 = imgUrl + fileName;
            }
        }

        feedBackRepository.save(request);
        return RestResult.ok(request);
    }



}
