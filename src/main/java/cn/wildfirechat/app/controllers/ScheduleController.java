package cn.wildfirechat.app.controllers;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.jpa.InfoTipsEntry;
import cn.wildfirechat.app.jpa.InfoTipsLogEntry;
import cn.wildfirechat.app.jpa.InfoTipsLogRepository;
import cn.wildfirechat.app.jpa.InfoTipsRepository;
import cn.wildfirechat.app.pojo.ScheduleDeleteRequest;
import cn.wildfirechat.app.pojo.ScheduleEditRequest;
import cn.wildfirechat.app.pojo.ScheduleQueryRequest;
import cn.wildfirechat.app.tools.Utils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 日程相关
 */
@Slf4j
@RestController
@RequestMapping("schedule")
public class ScheduleController {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleController.class);

    @Value("${local.media.temp_storage}")
    private String ossTempPath;

    @Value("${media.bucket_general_domain}")
    private String imgUrl;

    @Autowired
    private InfoTipsRepository infoTipsRepository;

    @Autowired
    private InfoTipsLogRepository infoTipsLogRepository;

    /**
     * 添加日程
     *
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTips/AddCustomerInfoTips", produces = "application/json;charset=UTF-8")
    public RestResult addCustomerInfoTips(@RequestBody InfoTipsEntry request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        request.user_id = userId;
        request.create_time = new Date();

        // 图片处理
        if(request.pic1 != null && request.pic1.length() > 120){
            String fileName = "Schedule/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.pic1,ossTempPath + fileName)){
                request.pic1 = imgUrl + fileName;
            }
        }
        if(request.pic2 != null && request.pic2.length() > 120){
            String fileName = "Schedule/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.pic2,ossTempPath + fileName)){
                request.pic2 = imgUrl + fileName;
            }
        }
        if(request.pic3 != null && request.pic3.length() > 120){
            String fileName = "Schedule/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.pic3,ossTempPath + fileName)){
                request.pic3 = imgUrl + fileName;
            }
        }

        infoTipsRepository.save(request);
        return RestResult.ok(request);
    }

    /**
     * 添加日程
     *
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTips/EditCustomerInfoTips", produces = "application/json;charset=UTF-8")
    public RestResult modifyInfoTips(@RequestBody ScheduleEditRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        //request.user_id = userId;
        InfoTipsEntry entry;

        // 图片处理
        if(request.pic1 != null && request.pic1.length() > 120){
            String fileName = "Schedule/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.pic1,ossTempPath + fileName)){
                request.pic1 = imgUrl + fileName;
            }
        }
        if(request.pic2 != null && request.pic2.length() > 120){
            String fileName = "Schedule/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.pic2,ossTempPath + fileName)){
                request.pic2 = imgUrl + fileName;
            }
        }
        if(request.pic3 != null && request.pic3.length() > 120){
            String fileName = "Schedule/" + userId + "-" + System.currentTimeMillis() + ".jpg";
            if(Utils.base64StrToImage(request.pic3,ossTempPath + fileName)){
                request.pic3 = imgUrl + fileName;
            }
        }

        Gson gson = new Gson();
        LOG.info("Login success " + gson.toJson(request));

        if(request.type==1) {
            // 记事
            entry = infoTipsRepository.modifyNotepad(request.id, request.title, request.repeat_setting, request.repeat_time, request.grade,
                    request.tag, request.type, request.is_open, request.address, request.remark, request.pic1, request.pic2, request.pic3);
        }else {
            // 日程
            entry = infoTipsRepository.modifyInfoTips(request.id,userId, request.title,request.tips_start_datetime,request.tips_end_datetime,request.is_full_day,
                    request.repeat_setting, request.repeat_time, request.grade, request.tag, request.type, request.is_open, request.address, request.remark,
                    request.pic1, request.pic2, request.pic3,request.edit_type,request.time);
        }
        return RestResult.ok(entry);
    }


    /**
     * 删除指定的日程
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTipsApi/DeleteInfoTipsById", produces = "application/json;charset=UTF-8")
    public RestResult removeInfoTipeItem(@RequestBody ScheduleDeleteRequest request) {
        //{"repeat_time":0,"id":632,"time":"0000-00-00","type":0}
        if(request.id == 0){
            return RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER);
        }
        infoTipsRepository.removeInfoTips(request.id,request.type,request.repeat_time,request.time);
        return RestResult.ok(null);
    }

    /**
     * 设置任务已完成
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTipsApi/SetInfoTipsFinish", produces = "application/json;charset=UTF-8")
    public RestResult setInfoTipsFinish(@RequestBody ScheduleDeleteRequest request) {
        if (request.type == 0){
            // 取消已完成
            infoTipsLogRepository.setInfoTipsNotFinish(request.id,request.time);
        }else {
            // 设置为已完成
            InfoTipsLogEntry entry=new InfoTipsLogEntry();
            entry.tips_id = request.id;
            entry.finish_time = request.time;
            entry.create_time = new Date();
            infoTipsLogRepository.save(entry);
        }
        return RestResult.ok(null);
    }

    /**
     * 设置任务已完成
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTipsApi/SetNotepadFinish", produces = "application/json;charset=UTF-8")
    public RestResult setNotepadFinish(@RequestBody ScheduleDeleteRequest request) {
        infoTipsRepository.setNotepadFinish(request.id,request.type);
        return RestResult.ok(null);
    }

    /**
     * 根据时间查询日程
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTipsApi/GetInfoTipsListByDate", produces = "application/json;charset=UTF-8")
    public Object getInfoTipsListByDate(@RequestBody ScheduleQueryRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        List<InfoTipsEntry> list = infoTipsRepository.loadInfoTipsByDate(userId, request.date + " 00:00:00",request.date + " 23:59:59");

        Gson gson = new Gson();
        LOG.info("请求信息 " + gson.toJson(request));
        LOG.info("日程信息 " + gson.toJson(list));
        return RestResult.ok(list);
    }

    /**
     * 根据 类型 查询日程
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTipsApi/GetInfoTipsList", produces = "application/json;charset=UTF-8")
    public Object getInfoTipsList(@RequestBody ScheduleQueryRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        //BaseResponse obj = new BaseResponse();
        //obj.code=200;
        //obj.data = infoTipsRepository.getInfoTipsList(userId, request.type,request.isfinish);
        //return obj;
        return RestResult.ok(infoTipsRepository.getInfoTipsList(userId, request.type,request.isfinish));
    }

}
