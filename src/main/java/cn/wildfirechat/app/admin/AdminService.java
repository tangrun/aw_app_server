package cn.wildfirechat.app.admin;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.admin.pojo.TextCardInfo;
import cn.wildfirechat.app.group.pojo.SolitaireInfo;
import cn.wildfirechat.app.group.pojo.SolitaireItemInfo;
import cn.wildfirechat.app.pojo.FriendExtraInfo;
import cn.wildfirechat.app.tools.Invoker;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import cn.wildfirechat.app.work.report.pojo.VOWorkReport;
import cn.wildfirechat.app.work.schedule.ScheduleEntity;
import cn.wildfirechat.app.work.schedule.ScheduleShareEntity;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.*;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.sdk.MessageAdmin;
import cn.wildfirechat.sdk.RelationAdmin;
import cn.wildfirechat.sdk.UserAdmin;
import cn.wildfirechat.sdk.model.IMResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;

@CacheConfig(cacheNames = "im-admin")
@Service
@Slf4j
public class AdminService {
    @Autowired
    AdminService adminService;

    /**
     * 发送日程分享消息
     * @param fromUser
     * @param conversation
     * @param shareEntity
     * @param entityList
     * @return
     */
    public ErrorCode sendScheduleShareMessage(String fromUser, Conversation conversation, ScheduleShareEntity shareEntity, List<ScheduleEntity> entityList) {
        // todo
        InputOutputUserInfo user = adminService.getUserById(fromUser);

        MessagePayload payload = new MessagePayload();
        payload.setType(MessageContentType.Type_Schedule_share);
        payload.setPersistFlag(ProtoConstants.PersistFlag.Persist_And_Count);
        // content
        {
            Map<String, Object> map = new HashMap<>();
            if (shareEntity.getType() == 0)
                map.put("title", String.format("%s的%s日程分享", user.getDisplayName(), new SimpleDateFormat("M月d日").format(new Date(shareEntity.getTime()))));
            else map.put("title", String.format("%s的待办分享", user.getDisplayName()));
            {
                StringBuilder stringBuilder = new StringBuilder();
                if (entityList.size() == 1) {
                    ScheduleEntity entity = entityList.get(0);
                    String startDate = entity.tips_start_datetime;
                    String endDate = entity.tips_end_datetime;
                    if (StringUtils.isNotBlank(startDate)) {
                        String[] strings = startDate.split(" ");
                        if (strings.length == 2) {
                            startDate = strings[1];
                        }
                    }
                    if (StringUtils.isNotBlank(endDate)) {
                        String[] strings = endDate.split(" ");
                        if (strings.length == 2) {
                            endDate = strings[1];
                        }
                    }
                    stringBuilder.append(entity.title)
                            .append("\n")
                            .append(startDate);
                    if (StringUtils.isNotBlank(endDate)) {
                        stringBuilder.append(" - ").append(endDate);
                    }
                } else {
                    for (int i = 0, i1 = Math.min(3, entityList.size()); i < i1; i++) {
                        ScheduleEntity entity = entityList.get(i);
                        if (i != 0) stringBuilder.append("\n");
                        stringBuilder.append(entity.title);
                    }
                }
                map.put("content", stringBuilder.toString());
            }
            map.put("icon", null);
            if (shareEntity.getType() == 0)
                map.put("tag", "日程");
            else map.put("tag", "待办");

            payload.setContent(new Gson().toJson(map));
        }
        {
            Map<String, Object> extra = new HashMap<>();

//            List<Long> ids = new ArrayList<>();
//            List<Map<String,Object>> extraEntityList = new ArrayList<>();
//            for (InfoTipsEntity entity : entityList) {
//                ids.add(entity.id);
//                Map<String,Object> map = new HashMap<>();
//                map.put("id",entity.id);
//                map.put("tips_start_datetime",entity.tips_start_datetime);
//                map.put("tips_end_datetime",entity.tips_end_datetime);
//                map.put("is_open",entity.is_open);
//                map.put("is_full_day",entity.is_full_day);
//                map.put("is_finish",entity.is_finish);
//                map.put("title",entity.title);
//                map.put("type",entity.type);
//                map.put("user_id",entity.user_id);
//                extraEntityList.add(map);
//            }
//            extra.put("list",extraEntityList);
//            extra.put("ids",ids);
            extra.put("id", shareEntity.getId());
            extra.put("type", shareEntity.getType());
            payload.setExtra(new Gson().toJson(extra));
        }

        return sendMessage(fromUser, conversation, payload);
    }

    /**
     * 发送日程接收信息
     * @param fromUser
     * @param conversation
     * @param scheduleEntity
     * @param isAdd true：添加、false：修改
     * @return
     */
    public ErrorCode sendScheduleReceiveMessage(String fromUser, Conversation conversation, ScheduleEntity scheduleEntity,boolean isAdd) {
        // todo
        InputOutputUserInfo user = adminService.getUserById(fromUser);
        if(user == null){
            return ErrorCode.ERROR_CODE_USER_FORBIDDEN;
        }

        MessagePayload payload = new MessagePayload();
        payload.setType(MessageContentType.Type_Schedule_receive);
        payload.setPersistFlag(ProtoConstants.PersistFlag.Persist_And_Count);
        // content
        {
            Map<String, Object> map = new HashMap<>();
            if (scheduleEntity.getType() == 0) {
                if(user == null){
                    map.put("title", "收到新日程，请查看！");
                }else {
                    if(isAdd) {
                        map.put("title", String.format("%s为您创建了日程！", user.getDisplayName()));
                    }else {
                        map.put("title", String.format("%s为您修改了日程！", user.getDisplayName()));
                    }
                }
            }else {
                map.put("title", String.format("%s的待办分享", user.getDisplayName()));
            }

            map.put("content", scheduleEntity.getTitle());

            map.put("icon", null);
            if (scheduleEntity.getType() == 0)
                map.put("tag", "日程");
            else map.put("tag", "待办");

            payload.setContent(new Gson().toJson(map));
        }
        {
            // 扩展消息
            //Map<String, Object> extra = new HashMap<>();
            //extra.put("id", scheduleEntity.getId());
            //extra.put("type", scheduleEntity.getType());
            //extra.put("title", scheduleEntity.getTitle());
            //extra.put("user_id", scheduleEntity.getUser_id());
            //extra.put("tips_start_datetime", scheduleEntity.getTips_start_datetime());
            //extra.put("tips_end_datetime", scheduleEntity.getTips_end_datetime());
            //extra.put("is_full_day", scheduleEntity.getIs_full_day());
            //extra.put("repeat_setting", scheduleEntity.getRepeat_setting());
            //extra.put("repeat_time", scheduleEntity.getRepeat_time());
            //extra.put("grade", scheduleEntity.getGrade());
            //extra.put("tag", scheduleEntity.getTag());
            //extra.put("is_open", scheduleEntity.getIs_open());
            //extra.put("address", scheduleEntity.getAddress());
            //extra.put("create_time", scheduleEntity.getCreate_time());
            //extra.put("remark", scheduleEntity.getRemark());
            //extra.put("pic1", scheduleEntity.getPic1());
            //extra.put("pic2", scheduleEntity.getPic2());
            //extra.put("pic3", scheduleEntity.getPic3());
            //extra.put("end_time", scheduleEntity.getEnd_time());
            //extra.put("is_finish", scheduleEntity.getIs_finish());
            //extra.put("forkId", scheduleEntity.getForkId());
            //extra.put("operatorId", scheduleEntity.getOperatorId());
            //payload.setExtra(new Gson().toJson(extra));
            Gson gson1 = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            payload.setExtra(gson1.toJson(scheduleEntity));
        }

        return sendMessage(fromUser, conversation, payload);
    }

    /**
     * 发送接龙消息
     * @param fromUser
     * @param groupId
     * @param solitaireInfo
     * @return
     */
    public ErrorCode sendSolitaireMessage(String fromUser, String groupId, SolitaireInfo solitaireInfo) {
        Conversation conversation = new Conversation();
        conversation.setTarget(groupId);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Group);
        MessagePayload payload = new MessagePayload();
        payload.setType(MessageContentType.Type_Group_Solitaire);
        payload.setPersistFlag(ProtoConstants.PersistFlag.Persist_And_Count);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(solitaireInfo.getTheme());
        if (StringUtils.isNotBlank(solitaireInfo.getTemplate())) {
            stringBuilder.append("\n").append(solitaireInfo.getTemplate());
        }
        if (solitaireInfo.getItems() != null) {
            stringBuilder.append("\n");
            int i = 0;
            for (SolitaireItemInfo item : solitaireInfo.getItems()) {
                stringBuilder.append("\n").append(++i).append(". ").append(item.getContent());
            }
        }
        if (StringUtils.isNotBlank(solitaireInfo.getSupply())) {
            stringBuilder.append("\n").append(solitaireInfo.getSupply());
        }
        payload.setContent(stringBuilder.toString());

        Map<String, Object> map = new HashMap<>();
        map.put("id", solitaireInfo.getId());
        payload.setExtra(new Gson().toJson(map));

        try {
            IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(fromUser, conversation, payload);
            if (resultSendMessage != null) return resultSendMessage.getErrorCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ErrorCode.ERROR_CODE_SERVER_ERROR;
    }

    /**
     * 发送普通文本消息
     *
     * @param fromUser
     * @param toUser
     * @param text
     */
    public ErrorCode sendTextMessage(String fromUser, String toUser, String text) {
        Conversation conversation = new Conversation();
        conversation.setTarget(toUser);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
        MessagePayload payload = new MessagePayload();
        payload.setType(MessageContentType.Type_Normal_Text);
        payload.setPersistFlag(ProtoConstants.PersistFlag.Persist_And_Count);
        payload.setSearchableContent(text);


        try {
            IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(fromUser, conversation, payload);
            if (resultSendMessage != null) return resultSendMessage.getErrorCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ErrorCode.ERROR_CODE_SERVER_ERROR;
    }

    private ErrorCode sendMessage(String fromUser, Conversation conversation, MessagePayload payload) {
        try {
            IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(fromUser, conversation, payload);
            if (resultSendMessage != null) return resultSendMessage.getErrorCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ErrorCode.ERROR_CODE_SERVER_ERROR;
    }

    @CacheEvict(key = "'user:id:'+#userId")
    public RestResult<Void> destroyUser(String userId){
        IMResult<Void> voidIMResult = null;
        try {
            voidIMResult = UserAdmin.destroyUser(userId);
            if (voidIMResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
                return RestResult.error(voidIMResult.getMsg());
            }else return RestResult.ok();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
    }

    @Cacheable(key = "'user:id:'+#userId", unless = "#result == null ")
    public InputOutputUserInfo getUserById(String userId) {
        IMResult<InputOutputUserInfo> result = null;
        try {
            if (StringUtils.isNotBlank(userId)) {
                result = UserAdmin.getUserByUserId(userId);
            }

            if (result.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                return result.getResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Cacheable(key = "'user:mobile:'+#mobile", unless = "#result == null ")
    @Nullable
    public InputOutputUserInfo getUserByMobile(String mobile) {
        IMResult<InputOutputUserInfo> result = null;
        try {
            if (StringUtils.isNotBlank(mobile)) {
                result = UserAdmin.getUserByMobile(mobile);
            }

            if (result.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                return result.getResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param fromUser
     * @param targetId
     * @param entry
     * @return
     */
    public RestResult sendWorkReportMessageToGroup(String fromUser, String targetId, VOWorkReport entry) {
        Conversation conversation = new Conversation();
        conversation.setTarget(targetId);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Group);
        MessagePayload payload = new MessagePayload();
        payload.setType(MessageContentType.Type_Work_Report_Send_To);
        payload.setPersistFlag(ProtoConstants.PersistFlag.Persist_And_Count);

        List<String> content = new ArrayList<>();
        content.add(String.format("提交时间：%s", DateFormatUtils.format(entry.getCreateTime(), "yyyy-MM-dd HH:mm")));
        WorkReportType type = WorkReportType.valueOf(entry.getType());
        switch (type) {
            case Report: {
                content.add("工作内容：" + entry.getJobContent());
                break;
            }
            case Daily: {
                content.add("今日工作：" + entry.getJobContent());
                content.add("明日计划：" + entry.getJobPlan());
                break;
            }
            case Week: {
                content.add("本周工作：" + entry.getJobContent());
                content.add("下周计划：" + entry.getJobPlan());
                break;
            }
            case Month: {
                content.add("本月工作：" + entry.getJobContent());
                content.add("下月计划：" + entry.getJobPlan());
                break;
            }
            case Year: {
                content.add("本年工作：" + entry.getJobContent());
                content.add("明年计划：" + entry.getJobPlan());
                break;
            }
        }
        payload.setContent(new Gson().toJson(TextCardInfo.builder()
                .title(String.format("%s的%s", getUserById(fromUser).getDisplayName(), type.getDesc()))
                .content(StringUtils.join(content, "\n"))
                .tag(type.getDesc())
                .icon(null)
                .build()));

        payload.setExtra(new Gson().toJson(entry));

        try {
            IMResult<SendMessageResult> result = MessageAdmin.sendMessage(fromUser, conversation, payload);
            return RestResult.result(result);
        } catch (Exception e) {
            e.printStackTrace();
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    /**
     * 设置用户扩展信息
     *
     * @param userId
     * @param targetId
     * @param invoker
     * @return
     */
    public RestResult setFriendExtra(String userId, String targetId, Invoker<FriendExtraInfo> invoker) {
        log.debug("setUserExtra() called with: userId = [{}], targetId = [{}]", userId, targetId);
        try {
            IMResult<RelationPojo> relation = RelationAdmin.getRelation(userId, targetId);
            if (relation.getCode() == ErrorCode.ERROR_CODE_SUCCESS.code) {
                RelationPojo result = relation.getResult();
                Gson gson = new Gson();
                FriendExtraInfo userExtraInfo = StringUtils.isBlank(result.extra) ? FriendExtraInfo.defaultExtra() : gson.fromJson(result.extra, FriendExtraInfo.class);
                invoker.onInvoke(userExtraInfo);
                IMResult<Void> result1 = RelationAdmin.updateFriendExtra(userId, targetId, gson.toJson(userExtraInfo));
                return RestResult.result(result1);
            }
            return RestResult.result(relation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
    }

    /**
     * 发送提示消息
     *
     * @param fromUser
     * @param targetUser
     * @param content
     * @return
     */
    public RestResult sendTipNotificationMessage(String fromUser, String targetUser, String content) {
        Conversation conversation = new Conversation();
        conversation.setTarget(targetUser);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
        MessagePayload payload = new MessagePayload();
        payload.setType(MessageContentType.Type_Tip_Notification);
        payload.setPersistFlag(ProtoConstants.PersistFlag.Persist);
        payload.setContent(content);
        try {
            IMResult<SendMessageResult> result = MessageAdmin.sendMessage(fromUser, conversation, payload);
            return RestResult.result(result);
        } catch (Exception e) {
            e.printStackTrace();
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    /**
     * 发送刷新好友列表消息 无显示
     *
     * @param fromUser
     * @param targetUser
     * @return
     */
    public RestResult sendRefreshFriendListNotifyMessage(String fromUser, String targetUser) {
        Conversation conversation = new Conversation();
        conversation.setTarget(targetUser);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
        MessagePayload payload = new MessagePayload();
        payload.setType(MessageContentType.Type_Refresh_Friend_List);
        payload.setPersistFlag(ProtoConstants.PersistFlag.Transparent);
        try {
            IMResult<SendMessageResult> result = MessageAdmin.sendMessage(fromUser, conversation, payload);
            return RestResult.result(result);
        } catch (Exception e) {
            e.printStackTrace();
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    /**
     * 删除消息
     *
     * @param messageUid
     * @return
     */
    public RestResult deleteMessage(long messageUid) {
        try {
            IMResult<Void> result = MessageAdmin.deleteMessage(messageUid);
            return RestResult.result(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
    }

    public boolean isUsernameAvailable(String username) {
        try {
            IMResult<InputOutputUserInfo> existUser = UserAdmin.getUserByName(username);
            if (existUser.code == ErrorCode.ERROR_CODE_NOT_EXIST.code) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
