package cn.wildfirechat.app.controllers;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.common.pojo.Page;
import cn.wildfirechat.app.jpa.NotepadEntry;
import cn.wildfirechat.app.jpa.NotepadRepository;
import cn.wildfirechat.app.pojo.LoadFavoriteRequest;
import cn.wildfirechat.app.pojo.LoadNotepadResponse;
import cn.wildfirechat.app.pojo.LoadNotepadTypeResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("notepad")
public class NotepadController {

    @Value("${local.media.temp_storage}")
    private String ossTempPath;

    @Value("${media.bucket_general_domain}")
    private String imgUrl;

    @Autowired
    private NotepadRepository notepadRepository;

    /**
     * 添加记事
     *
     * @param request
     * @return
     */
    @PostMapping(value = "notepad/add", produces = "application/json;charset=UTF-8")
    public RestResult AddNotepad(@RequestBody NotepadEntry request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        request.uid = userId;
        request.create_time = new Date();

        //// 图片处理
        //if(request.attach_file != null && request.attach_file.length() > 60){
        //    String fileName = "feedback/" + userId + "-" + System.currentTimeMillis() + ".jpg";
        //    if(Utils.base64StrToImage(request.attach_file,ossTempPath + fileName)){
        //        request.attach_file = imgUrl + fileName;
        //    }
        //}

        if (StringUtils.isEmpty(request.content)) {
            return RestResult.result(1,"内容不能为空！",null);
        }

        notepadRepository.save(request);
        return RestResult.ok(request);
    }

    /**
     * 修改记事
     *
     * @param request
     * @return
     */
    @PostMapping(value = "notepad/modify", produces = "application/json;charset=UTF-8")
    public RestResult modifyNotepad(@RequestBody NotepadEntry request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        request.uid = userId;
        //// 图片处理
        //if(request.pic1 != null && request.pic1.length() > 120){
        //    String fileName = "Schedule/" + userId + "-" + System.currentTimeMillis() + ".jpg";
        //    if(Utils.base64StrToImage(request.pic1,ossTempPath + fileName)){
        //        request.pic1 = imgUrl + fileName;
        //    }
        //}
        //if(request.pic2 != null && request.pic2.length() > 120){
        //    String fileName = "Schedule/" + userId + "-" + System.currentTimeMillis() + ".jpg";
        //    if(Utils.base64StrToImage(request.pic2,ossTempPath + fileName)){
        //        request.pic2 = imgUrl + fileName;
        //    }
        //}

        if(request.id == 0){
            return RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER);
        }

        if (StringUtils.isEmpty(request.content)) {
            return RestResult.result(1,"内容不能为空！",null);
        }

        notepadRepository.save(request);
        return RestResult.ok(request);
    }

    /**
     * 删除指定的记事
     * @param request
     * @return
     */
    @PostMapping(value = "notepad/del", produces = "application/json;charset=UTF-8")
    public RestResult removeNotepad(@RequestBody NotepadEntry request) {
        if(request.id == 0){
            return RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER);
        }
        notepadRepository.deleteById(request.id);
        return RestResult.ok(null);
    }

    /**
     * 删除指定的记事
     * @param request
     * @return
     */
    @PostMapping(value = "notepad/istop", produces = "application/json;charset=UTF-8")
    public RestResult setNotepadTop(@RequestBody NotepadEntry request) {
        if(request.id == 0){
            return RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER);
        }
        notepadRepository.setNotepadTop(request.id,request.isTop);
        return RestResult.ok(null);
    }


    /**
     * 加载记事本列表
     * @param request
     * @return
     */
    @PostMapping(value = "notepad/list", produces = "application/json;charset=UTF-8")
    public Object getNotepadItems(@RequestBody LoadFavoriteRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        long id= request.id;
        id = id > 0 ? id : Long.MAX_VALUE;
        List<NotepadEntry> notes;
        if(request.type >= 0){
            //notes = notepadRepository.loadNotepad(userId, id, request.count,request.type);
            Page page = new Page();
            page.setPage(Math.toIntExact(request.id));
            page.setSize(request.count);
            notes = notepadRepository.getNotepadListByType(userId,request.type,page.convert2PageRequest());
        }else {
            //notes = notepadRepository.loadNotepad(userId, id, request.count);
            Page page = new Page();
            page.setPage(Math.toIntExact(request.id));
            page.setSize(request.count);
            notes = notepadRepository.getNotepadAllList(userId,page.convert2PageRequest());
        }
        LoadNotepadResponse response = new LoadNotepadResponse();
        response.items = notes;
        response.hasMore = notes.size() == request.count;
        return RestResult.ok(response);
    }

    /**
     * 加载记事本分类列表
     * @return
     */
    @PostMapping(value = "notepad/totalList", produces = "application/json;charset=UTF-8")
    public Object getNotepadTotalItems() {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        List<Map<String,Object>> notes = notepadRepository.loadNotepadTotal(userId);
        LoadNotepadTypeResponse response = new LoadNotepadTypeResponse();
        response.items = notes;
        return RestResult.ok(response);
    }

}
