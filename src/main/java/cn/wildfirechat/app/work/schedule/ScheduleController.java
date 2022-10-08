package cn.wildfirechat.app.work.schedule;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.common.CommonService;
import cn.wildfirechat.app.common.consts.SessionAttributes;
import cn.wildfirechat.app.work.schedule.pojo.*;
import cn.wildfirechat.pojos.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.*;

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
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleLogRepository scheduleLogRepository;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    AdminService adminService;

    @Autowired
    ScheduleShareRepository scheduleShareRepository;

    @Autowired
    CommonService commonService;

    @Autowired
    SchedulePermissionService permissionService;

//    /**
//     * 更新权限设置
//     * @param userId
//     * @param request
//     * @return
//     */
//    @PostMapping("permission/update")
//    public RestResult<Void> permission(
//            @SessionAttribute(SessionAttributes.userId) String userId,
//            @RequestBody SchedulePermissionVO request
//    ) {
//        return scheduleService.setPermission(userId, request);
//    }
//
//    /**
//     * 某人对我的权限设置
//     * @param userId
//     * @return
//     */
//    @PostMapping("userPermission")
//    @Validated
//    public RestResult<ScheduleUserPermissionResponse> userPermission(
//            @SessionAttribute(SessionAttributes.userId) String userId,
//            @RequestBody ScheduleUserPermissionRequest request
//    ) {
//        return scheduleService.getUserPermission(userId,request);
//    }
//
//    /**
//     * 我的权限设置
//     * @param userId
//     * @return
//     */
//    @PostMapping("permission")
//    public RestResult<SchedulePermissionVO> permission(@SessionAttribute(SessionAttributes.userId) String userId
//    ) {
//        return scheduleService.getPermission(userId);
//    }

    @PostMapping("share/fork")
    public RestResult<Void> scheduleShareFork(@SessionAttribute String userId, @RequestParam Long id, @RequestParam List<Long> cid) {
        return scheduleService.fork(userId, id, cid);
    }

    @PostMapping("share/detail")
    public RestResult<ScheduleShareVO> getScheduleInfos(@SessionAttribute(SessionAttributes.userId) String userId, @RequestParam Long id) {
        return scheduleService.getScheduleShareInfo(id);
    }

    @PostMapping("share")
    @Validated
    public RestResult<Void> share(@SessionAttribute(SessionAttributes.userId) String userId,
                                  @NotNull @RequestParam Long time, @NotNull @RequestParam List<Long> ids, @NotNull @RequestParam Integer type,
                                  @NotNull @RequestParam String convTarget, @NotNull @RequestParam Integer convLine, @NotNull @RequestParam Integer convType
    ) {
        Conversation conversation = new Conversation();
        conversation.setTarget(convTarget);
        conversation.setLine(convLine);
        conversation.setType(convType);
        ScheduleShareRequest request = new ScheduleShareRequest();
        request.setTime(time);
        request.setIds(ids);
        request.setType(type);
        request.setConversationList(Collections.singletonList(conversation));
        return scheduleService.share(userId, request);
    }

    @PostMapping("share2")
    public RestResult<Void> share(@SessionAttribute(SessionAttributes.userId) String userId,
                                  @Validated @RequestBody ScheduleShareRequest request
    ) {
        return scheduleService.share(userId, request);
    }

    /**
     * 添加日程
     *
     * @return
     */
    @PostMapping(value = "InfoTips/AddCustomerInfoTips")
    public RestResult<ScheduleInfo> addCustomerInfoTips(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody ScheduleEditRequest scheduleInfo) {

        return scheduleService.addSchedule(userId, scheduleInfo);
    }

    /**
     * 添加日程
     *
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTips/EditCustomerInfoTips")
    public RestResult modifyInfoTips(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody ScheduleEditRequest request) {
        return scheduleService.modifySchedule(userId, request);
    }


    /**
     * 删除指定的日程
     *
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTipsApi/DeleteInfoTipsById", produces = "application/json;charset=UTF-8")
    public RestResult<Void> removeInfoTipeItem(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody ScheduleDeleteRequest request) {
        return scheduleService.delete(userId, request);
    }

    /**
     * 设置任务已完成
     *
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTipsApi/SetInfoTipsFinish", produces = "application/json;charset=UTF-8")
    public RestResult<Void> setInfoTipsFinish(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody ScheduleDeleteRequest request) {
        return scheduleService.finish(userId, 0, request);
    }

    /**
     * 设置任务已完成
     *
     * @param request
     * @return
     */
    @PostMapping(value = "InfoTipsApi/SetNotepadFinish", produces = "application/json;charset=UTF-8")
    public RestResult setNotepadFinish(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody ScheduleDeleteRequest request) {
        return scheduleService.finish(userId, 1, request);
    }

    /**
     * 日程
     *
     * @return
     */
    @PostMapping(value = "InfoTipsApi/GetInfoTipsListByDate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Validated
    public RestResult<List<ScheduleInfo>> getInfoTipsListByDate_Json(
            @SessionAttribute(SessionAttributes.userId) String loginUserId,
            @RequestBody ScheduleQueryRequest request
    ) {
        if (StringUtils.isNotBlank(request.getUserId())) {
            return scheduleService.getScheduleDayList(request.getUserId(), request.getDate(), loginUserId);
        } else return scheduleService.getScheduleDayList(loginUserId, request.getDate(), null);
    }

    @PostMapping(value = "InfoTipsApi/GetInfoTipsListByDate")
    @Validated
    public RestResult<List<ScheduleInfo>> getInfoTipsListByDate(
            @SessionAttribute(SessionAttributes.userId) String loginUserId,
            ScheduleQueryRequest request
    ) {
        if (StringUtils.isNotBlank(request.getUserId())) {
            return scheduleService.getScheduleDayList(request.getUserId(), request.getDate(), loginUserId);
        } else return scheduleService.getScheduleDayList(loginUserId, request.getDate(), null);
    }


    /**
     * 代办
     *
     * @return
     */
    @PostMapping(value = "InfoTipsApi/GetInfoTipsList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Validated
    public RestResult<List<ScheduleInfo>> getInfoTipsList_Json(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody TodoQueryRequest request
    ) {
        return scheduleService.getToDoList(userId, request);
    }

    @PostMapping(value = "InfoTipsApi/GetInfoTipsList")
    @Validated
    public RestResult<List<ScheduleInfo>> getInfoTipsList(
            @SessionAttribute(SessionAttributes.userId) String userId,
            TodoQueryRequest request
    ) {
        return scheduleService.getToDoList(userId, request);
    }


}
