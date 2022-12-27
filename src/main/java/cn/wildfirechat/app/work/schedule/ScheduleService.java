package cn.wildfirechat.app.work.schedule;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.UserService;
import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.common.CommonService;
import cn.wildfirechat.app.common.entity.UploadFile;
import cn.wildfirechat.app.jpa.UserEntity;
import cn.wildfirechat.app.jpa.UserEntityRepository;
import cn.wildfirechat.app.work.schedule.pojo.*;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.Conversation;
import cn.wildfirechat.pojos.SendMessageResult;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.sdk.model.IMResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ikidou.reflect.TypeToken;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ScheduleLogRepository scheduleLogRepository;
    @Autowired
    ScheduleShareRepository scheduleShareRepository;
    @Autowired
    UserService userService;
    @Autowired
    AdminService adminService;
    @Autowired
    CommonService commonService;
    @Autowired
    UserEntityRepository userEntityRepository;

    @Autowired
    SchedulePermissionRepository permissionRepository;

    /**
     * @param loginUserId
     * @param type        0日程 1待办
     * @param request
     * @return
     */
    public RestResult<Void> finish(String loginUserId, int type, ScheduleDeleteRequest request) {
        String userId, operatorId = null;
        if (StringUtils.isNotBlank(request.getUserId())) {
            userId = request.getUserId();
            operatorId = loginUserId;
            // 别人操作的
            if (!checkOperatorPermission(userId, operatorId, false)) {
                return RestResult.error("你没有操作权限");
            }
        } else {
            userId = loginUserId;
        }

        if (type == 0) {
            // 日程
            if (request.type == 0) {
                // 取消已完成
                scheduleLogRepository.setInfoTipsNotFinish(request.id, request.time);
            } else {
                // 设置为已完成
                ScheduleLogEntity entry = new ScheduleLogEntity();
                entry.tips_id = request.id;
                entry.finish_time = request.time;
                entry.create_time = new Date();
                scheduleLogRepository.save(entry);
            }
        } else if (type == 1) {
            // 待办
            scheduleRepository.setNotepadFinish(request.id, request.type);
        }

        return RestResult.ok(null);
    }

    /**
     * 删除日程信息
     * @param loginUserId
     * @param request
     * @return
     */
    public RestResult<Void> delete(String loginUserId, ScheduleDeleteRequest request) {
        String userId, operatorId = null;
        if (StringUtils.isNotBlank(request.getUserId())) {
            userId = request.getUserId();
            operatorId = loginUserId;
            // 别人操作的
            if (!checkOperatorPermission(userId, operatorId, false)) {
                return RestResult.error("你没有操作权限");
            }
        } else {
            userId = loginUserId;
        }

        if (request.id == 0) {
            return RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER);
        }
        scheduleRepository.removeInfoTips(request.id, request.type, request.repeat_time, request.time);
        return RestResult.ok(null);
    }

    /**
     * 编辑日程信息
     * @param loginUserId
     * @param request
     * @return
     */
    public RestResult<ScheduleInfo> modifySchedule(String loginUserId, ScheduleEditRequest request) {
        String userId, operatorId = null;
        if (StringUtils.isNotBlank(request.getUserId())) {
            userId = request.getUserId();
            operatorId = loginUserId;
            // 别人操作的
            if (!checkOperatorPermission(userId, operatorId, false)) {
                return RestResult.error("你没有操作权限");
            }
        } else {
            userId = loginUserId;
        }

        ScheduleEntity entry;

        // 图片处理
        if (request.pic1 != null && request.pic1.length() > 120) {
            UploadFile uploadFile = commonService.uploadBase64File(userId, request.pic1);
            request.pic1 = null;
            if (uploadFile != null) {
                request.pic1 = commonService.getDownloadPath2(uploadFile);
            }
        }
        if (request.pic2 != null && request.pic2.length() > 120) {
            UploadFile uploadFile = commonService.uploadBase64File(userId, request.pic2);
            request.pic2 = null;
            if (uploadFile != null) {
                request.pic2 = commonService.getDownloadPath2(uploadFile);
            }
        }
        if (request.pic3 != null && request.pic3.length() > 120) {
            UploadFile uploadFile = commonService.uploadBase64File(userId, request.pic3);
            request.pic3 = null;
            if (uploadFile != null) {
                request.pic3 = commonService.getDownloadPath2(uploadFile);
            }
        }

        if (request.type == 1) {
            // 记事
            entry = scheduleRepository.modifyNotepad(request.id, request.title, request.repeat_setting, request.repeat_time, request.grade,
                    request.tag, request.type, request.is_open, request.address, request.remark, request.pic1, request.pic2, request.pic3);
        } else if (request.type == 0 || request.type == 3) {
            // 日程
            entry = scheduleRepository.modifyInfoTips(request.id, userId, request.title, request.tips_start_datetime,
                    StringUtils.isNotBlank(request.tips_end_datetime) ? request.tips_end_datetime : request.tips_start_datetime,// 客户端修改时传空就查不到
                    request.is_full_day, request.repeat_setting, request.repeat_time, request.grade, request.tag, request.type, request.is_open, request.address, request.remark,
                    request.pic1, request.pic2, request.pic3, request.edit_type, request.time);

            // 推送消息给对方 提示对方接收
            if (StringUtils.isNotBlank(request.getUserId())) {
                Conversation conversation = new Conversation();
                conversation.setTarget(userId);
                conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
                IMResult<SendMessageResult> result = adminService.sendScheduleReceiveMessage(loginUserId, conversation, entry, false);
                return result.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS ? RestResult.ok(convertTo(entry)) : RestResult.result(result);
            }
        } else {
            return RestResult.error("暂不支持修改");
        }
        return RestResult.ok(convertTo(entry));
    }

    /**
     * 添加日程
     *
     * @param loginUserId
     * @param scheduleInfo
     * @return
     */
    public RestResult<ScheduleInfo> addSchedule(String loginUserId, ScheduleEditRequest scheduleInfo) {
        ScheduleEntity entity = new ScheduleEntity();

        String userId, operatorId = null;
        if (StringUtils.isNotBlank(scheduleInfo.getUserId())) {
            userId = scheduleInfo.getUserId();
            operatorId = loginUserId;
            // 别人操作的
            if (!checkOperatorPermission(userId, operatorId, false)) {
                return RestResult.error("你没有操作权限");
            }
        } else {
            userId = loginUserId;
        }

        BeanUtils.copyProperties(scheduleInfo, entity);

        if (StringUtils.isNotBlank(operatorId)) {
            if (scheduleInfo.type != 0) {
                return RestResult.error("暂只支持日程的操作");
            }
            scheduleInfo.type = 3;
            entity.setOperatorId(operatorId);
        }

        entity.user_id = userId;
        entity.create_time = new Date();

        // 图片处理
        if (entity.pic1 != null && entity.pic1.length() > 120) {
            UploadFile uploadFile = commonService.uploadBase64File(userId, entity.pic1);
            entity.pic1 = null;
            if (uploadFile != null) {
                entity.pic1 = commonService.getDownloadPath2(uploadFile);
            }
        }
        if (entity.pic2 != null && entity.pic2.length() > 120) {
            UploadFile uploadFile = commonService.uploadBase64File(userId, entity.pic2);
            entity.pic2 = null;
            if (uploadFile != null) {
                entity.pic2 = commonService.getDownloadPath2(uploadFile);
            }
        }
        if (entity.pic3 != null && entity.pic3.length() > 120) {
            UploadFile uploadFile = commonService.uploadBase64File(userId, entity.pic3);
            entity.pic3 = null;
            if (uploadFile != null) {
                entity.pic3 = commonService.getDownloadPath2(uploadFile);
            }
        }

        // 保存日程信息
        entity = scheduleRepository.saveAndFlush(entity);
        if(entity == null){
            return RestResult.error("保存失败，请稍后重试！");
        }

        // 推送消息给对方 提示对方接收
        if (StringUtils.isNotBlank(scheduleInfo.getUserId())) {
            Conversation conversation = new Conversation();
            conversation.setTarget(scheduleInfo.getUserId());
            conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
            IMResult<SendMessageResult> result = adminService.sendScheduleReceiveMessage(loginUserId, conversation, entity, true);
            return result.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS ? RestResult.ok(convertTo(entity)) : RestResult.result(result);
        }
        return RestResult.ok(convertTo(entity));
    }

    /**
     * 检查日程权限
     * @param userId 目标人
     * @param operatorId 操作人
     * @param read true读权限 false写权限
     * @return
     */
    private boolean checkOperatorPermission(String userId, String operatorId, boolean read) {
        //Pair<Boolean, Boolean> pair = checkOperatorPermission(userId, operatorId);
        //return read?pair.getKey():pair.getValue();

        SchedulePermissionEntity entity = permissionRepository.findUserSet(userId,operatorId);
        if(entity == null){
            return false;
        }
        if(read){
            // 读权限
            return entity.look != 0;
        }else {
            // 写权限
            return entity.edit != 0;
        }
    }

    /**
     * 获取待办列表
     * @param userId 用户Id
     * @param request 是否完成
     * @return
     */
    public RestResult<List<ScheduleInfo>> getToDoList(@Nonnull String userId, TodoQueryRequest request) {
        List<ScheduleEntity> list = scheduleRepository.findAllByUser_idAndTypeAndIs_finish(userId, request.getType()==null?1:request.getType(), request.getIsfinish());
        List<ScheduleInfo> scheduleInfoList = list.stream().map(this::convertTo).collect(Collectors.toList());
        return RestResult.ok(scheduleInfoList);
    }

    /**
     * 获取日程当天
     *
     * @param userId
     * @param day        yyyy:MM:dd
     * @param operatorId
     * @return
     */
    public RestResult<List<ScheduleInfo>> getScheduleDayList(@Nonnull String userId, @Nonnull String day, @Nullable String operatorId) {
        List<ScheduleEntity> list = null;
        if (StringUtils.isNotBlank(operatorId)) {
            Assert.isTrue(checkOperatorPermission(userId, operatorId, true), "您没有查看权限");
            list = scheduleRepository.loadInfoTipsByDate(userId, day + " 00:00:00", day + " 23:59:59", operatorId);
        } else {
            list = scheduleRepository.loadInfoTipsByDate(userId, day + " 00:00:00", day + " 23:59:59");
        }

        List<ScheduleInfo> scheduleInfoList = list.stream().map(this::convertTo).collect(Collectors.toList());
        return RestResult.ok(scheduleInfoList);
    }

    /**
     * 从分享添加
     *
     * @param userId
     * @param id
     * @param cid
     * @return
     */
    public RestResult<Void> fork(String userId, Long id, List<Long> cid) {
        Optional<ScheduleShareEntity> optional = scheduleShareRepository.findById(id);
        if (!optional.isPresent()) {
            return RestResult.error("日程分享不存在");
        }
        ScheduleShareEntity scheduleShareEntity = optional.get();
        List<Long> sharedIdList = new Gson().fromJson(scheduleShareEntity.getIds(), new TypeToken<List<Long>>() {
        }.getType());
        List<Long> containIdList = new ArrayList<>();
        for (Long aLong : cid) {
            if (sharedIdList.contains(aLong))
                containIdList.add(aLong);
        }
        if (containIdList.isEmpty())
            return RestResult.error("请先选择要添加的日程");
        // 已经添加过的
        List<Long> forkedIdList = scheduleRepository.findAllByUser_idAndForkIdIn(userId, containIdList).stream()
                .map(entity -> entity.id)
                .collect(Collectors.toList());
        if (forkedIdList.size() == containIdList.size())
            return RestResult.error("所选的都已经添加过了");
        List<Long> forkIdList = containIdList.stream()
                .filter(aLong -> !forkedIdList.contains(aLong))
                .collect(Collectors.toList());

        List<ScheduleEntity> forkScheduleEntityList = scheduleRepository.findAllById(forkIdList);
        List<ScheduleEntity> saveEntityList = new ArrayList<>();
        for (ScheduleEntity scheduleEntity : forkScheduleEntityList) {
            ScheduleEntity entity = new ScheduleEntity();
            BeanUtils.copyProperties(scheduleEntity, entity);
            entity.user_id = userId;
            entity.type = scheduleEntity.type == 3 ? 0 : scheduleEntity.type;
            entity.create_time = new Date();
            entity.remark = "";
            entity.address = "";
            entity.forkId = scheduleEntity.id;
            saveEntityList.add(entity);
        }
        scheduleRepository.saveAll(saveEntityList);
        return RestResult.ok(null);
    }

    /**
     * 分享
     */
    public RestResult<Void> share(String loginUserId, ScheduleShareRequest request) {
        String userId, operatorId = null;
        if (StringUtils.isNotBlank(request.getUserId())) {
            userId = request.getUserId();
            operatorId = loginUserId;
            // 别人操作的
            if (!checkOperatorPermission(userId, operatorId, false)) {
                return RestResult.error("你没有操作权限");
            }
        } else {
            userId = loginUserId;
        }

        List<ScheduleEntity> entityList = scheduleRepository.findAllById(request.getIds());
        Assert.isTrue(!entityList.isEmpty(), "日程数量为空");

        ScheduleShareEntity scheduleShareEntity = ScheduleShareEntity.builder()
                .userId(userId)
                .type(request.getType())
                .time(request.getTime())
                .ids(new Gson().toJson(entityList.stream()
                        .map(new Function<ScheduleEntity, Long>() {
                            @Override
                            public Long apply(ScheduleEntity infoTipsEntity) {
                                return infoTipsEntity.id;
                            }
                        })
                        .collect(Collectors.toList())
                ))
                .build();
        scheduleShareRepository.saveAndFlush(scheduleShareEntity);

        ErrorCode errorCode = ErrorCode.ERROR_CODE_SUCCESS;
        for (Conversation conversation : request.getConversationList()) {
            adminService.sendScheduleShareMessage(userId, conversation, scheduleShareEntity, entityList);
        }

        return  RestResult.ok(null);
    }

    /**
     * 分享详情
     *
     * @param id
     * @return
     */
    public RestResult<ScheduleShareVO> getScheduleShareInfo(Long id) {
        Optional<ScheduleShareEntity> optional = scheduleShareRepository.findById(id);
        Assert.isTrue(optional.isPresent(), "分享不存在");

        ScheduleShareEntity scheduleShareEntity = optional.get();
        List<Long> ids = new Gson().fromJson(scheduleShareEntity.getIds(), new TypeToken<List<Long>>() {
        }.getType());

        List<ScheduleEntity> entityList = getScheduleInfoListByIds(ids);
        Assert.isTrue(!entityList.isEmpty(), "该分享已被取消");

        ScheduleShareVO scheduleShareVO = new ScheduleShareVO()
                .setId(scheduleShareEntity.getId())
                .setUserId(scheduleShareEntity.getUserId())
                .setIds(ids)
                .setType(scheduleShareEntity.getType())
                .setCreateTime(scheduleShareEntity.getType() == 0 && scheduleShareEntity.getTime() > 0 ? new Date(scheduleShareEntity.getTime())
                        : scheduleShareEntity.getCreateTime())
                .setScheduleInfoList(entityList);
        return RestResult.ok(scheduleShareVO);
    }

    public List<ScheduleEntity> getScheduleInfoListByIds(List<Long> ids) {
        return scheduleRepository.findAllById(ids);
    }

    private ScheduleInfo convertTo(ScheduleEntity entity) {
        ScheduleInfo scheduleInfo = new ScheduleInfo();
        BeanUtils.copyProperties(entity, scheduleInfo);
        return scheduleInfo;
    }






//    /**
//     * 某人对我的权限设置
//     * @return
//     */
//    public RestResult<ScheduleUserPermissionResponse> getUserPermission(String userId,ScheduleUserPermissionRequest request) {
//        Pair<Boolean, Boolean> pair = checkOperatorPermission(request.getUserId(),userId);
//        ScheduleUserPermissionResponse response = new ScheduleUserPermissionResponse();
//        response.setReadable(pair.getKey());
//        response.setWriteable(pair.getValue());
//        return RestResult.ok(response);
//    }
//
//    /**
//     * 我的权限设置
//     * @param userId
//     * @return
//     */
//    public RestResult<SchedulePermissionVO> getPermission(String userId) {
//        SchedulePermissionVO permissionSetting = getPermissionSetting(userId);
//        Assert.notNull(permissionSetting,"用户不存在");
//        return RestResult.ok(permissionSetting);
//    }
//
//    /**
//     * 我的权限设置
//     * @param userId
//     * @return
//     */
//    private SchedulePermissionVO getPermissionSetting(String userId) {
//        //UserEntity userEntity = userService.findByUserId(userId);
//        UserEntity userEntity = userEntityRepository.findFirstByUserId(userId);
//        //System.out.println("用户设置：" + new Gson().toJson(userEntity));
//        if (userEntity == null) return null;
//        SchedulePermissionVO vo = new SchedulePermissionVO();
//        int read = SchedulePermission.getRead(userEntity.getSchedulePermission());
//        int write = SchedulePermission.getWrite(userEntity.getSchedulePermission());
//        vo.setRead(read);
//        vo.setWrite(write);
//        Gson gson = new Gson();
//        if (StringUtils.isNotBlank(userEntity.getSchedulePermissionExtra())) {
//            JsonObject jsonObject = gson.fromJson(userEntity.getSchedulePermissionExtra(), JsonObject.class);
//            JsonElement r = jsonObject.get("r");
//            if (r != null)
//                vo.setReadableUserIdList(gson.fromJson(r, new TypeToken<List<String>>() {
//                }.getType()));
//            JsonElement w = jsonObject.get("w");
//            if (w != null)
//                vo.setWritableUserIdList(gson.fromJson(w, new TypeToken<List<String>>() {
//                }.getType()));
//        }
//        return vo;
//    }
//
//    /**
//     * 保存权限设置
//     * @param userId
//     * @param request
//     * @return
//     */
//    public RestResult<Void> setPermission(String userId, SchedulePermissionVO request) {
//        UserEntity userEntity = userService.findByUserId(userId);
//        Assert.notNull(userEntity, "用户不存在");
//        Assert.isTrue(SchedulePermission.checkValue(request.getRead()), "权限不正确");
//        Assert.isTrue(SchedulePermission.checkValue(request.getWrite()), "权限不正确");
//
//        int value = SchedulePermission.setReadWrite(request.getRead(), request.getWrite());
//
//        Gson gson = new Gson();
//        JsonObject jsonObject = new JsonObject();
//        if (request.getRead() == SchedulePermission.Part)
//            jsonObject.add("r", gson.toJsonTree(request.getReadableUserIdList()));
//        else jsonObject.add("r", null);
//        if (request.getWrite() == SchedulePermission.Part)
//            jsonObject.add("w", gson.toJsonTree(request.getWritableUserIdList()));
//        else jsonObject.add("w", null);
//
//        userEntity.setSchedulePermission(value);
//        userEntity.setSchedulePermissionExtra(jsonObject.toString());
//        userService.saveUserEntity(userEntity);
//        return RestResult.ok();
//    }
//
//    /**
//     * 检查日程权限
//     * @param userId 目标人
//     * @param operatorId 操作人
//     * @return left read right write
//     */
//    private Pair<Boolean,Boolean> checkOperatorPermission(String userId, String operatorId) {
//        SchedulePermissionVO permission = getPermissionSetting(userId);
//        if (permission == null) return new Pair<>(false,false);
//        System.out.println("我的ID：" + userId + "  好友ID：" + operatorId);
//        System.out.println("权限设置：" + new Gson().toJson(permission));
//
//        boolean readable = false, writeable = false;
//        {
//            if (permission.getRead() == SchedulePermission.Public) {
//                readable = true;
//            }
//            if (permission.getRead() == SchedulePermission.Part) {
//                readable = permission.getReadableUserIdList() != null && permission.getReadableUserIdList().contains(operatorId);
//            }
//        }
//        {
//            if (permission.getWrite() == SchedulePermission.Public) {
//                writeable = true;
//            }
//            if (permission.getWrite() == SchedulePermission.Part) {
//                writeable = permission.getWritableUserIdList() != null && permission.getWritableUserIdList().contains(operatorId);
//            }
//        }
//        return new Pair<>(writeable || readable,writeable);
//    }


}
