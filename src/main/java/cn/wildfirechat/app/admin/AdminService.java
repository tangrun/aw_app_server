package cn.wildfirechat.app.admin;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.admin.pojo.TextCardInfo;
import cn.wildfirechat.app.group.pojo.SolitaireInfo;
import cn.wildfirechat.app.group.pojo.SolitaireItemInfo;
import cn.wildfirechat.app.pojo.FriendExtraInfo;
import cn.wildfirechat.app.tools.Invoker;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import cn.wildfirechat.app.work.report.pojo.VOWorkReport;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.*;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.sdk.MessageAdmin;
import cn.wildfirechat.sdk.RelationAdmin;
import cn.wildfirechat.sdk.UserAdmin;
import cn.wildfirechat.sdk.model.IMResult;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CacheConfig(cacheNames = "im-admin")
@Service
@Slf4j
public class AdminService {

    public ErrorCode sendSolitaireMessage(String fromUser, String groupId, SolitaireInfo solitaireInfo){
        Conversation conversation = new Conversation();
        conversation.setTarget(groupId);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Group);
        MessagePayload payload = new MessagePayload();
        payload.setType(MessageContentType.Type_Group_Solitaire);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(solitaireInfo.getTheme());
        if (StringUtils.isNotBlank(solitaireInfo.getTemplate())) {
            stringBuilder.append("\n").append(solitaireInfo.getTemplate());
        }
        if (solitaireInfo.getItems()!=null){
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

        Map<String,Object> map = new HashMap<>();
        map.put("id", solitaireInfo.getId());
        payload.setExtra(new Gson().toJson(map));

        try {
            IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(fromUser, conversation, payload);
            if (resultSendMessage != null)return resultSendMessage.getErrorCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ErrorCode.ERROR_CODE_SERVER_ERROR;
    }

    /**
     * 发送普通文本消息
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
        payload.setSearchableContent(text);


        try {
            IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(fromUser, conversation, payload);
            if (resultSendMessage != null)return resultSendMessage.getErrorCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ErrorCode.ERROR_CODE_SERVER_ERROR;
    }

    @Cacheable(key = "'user:id:'+#userId",unless = "#result == null ")
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

    @Cacheable(key = "'user:mobile:'+#mobile",unless = "#result == null ")
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
     *
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
        content.add(   String.format("提交时间：%s", DateFormatUtils.format(entry.getCreateTime(), "yyyy-MM-dd HH:mm")));
        WorkReportType type = WorkReportType.valueOf(entry.getType());
        switch (type){
            case Report:{
                content.add("工作内容："+entry.getJobContent());
                break;
            }
            case Daily:{
                content.add("今日工作："+entry.getJobContent());
                content.add("明日计划："+entry.getJobPlan());
                break;
            }
            case Week:{
                content.add("本周工作："+entry.getJobContent());
                content.add("下周计划："+entry.getJobPlan());
                break;
            }
            case Month:{
                content.add("本月工作："+entry.getJobContent());
                content.add("下月计划："+entry.getJobPlan());
                break;
            }
            case Year:{
                content.add("本年工作："+entry.getJobContent());
                content.add("明年计划："+entry.getJobPlan());
                break;
            }
        }
        payload.setContent(new Gson().toJson(TextCardInfo.builder()
                .title(String.format("%s的%s", getUserById(fromUser).getDisplayName(),type.getDesc()))
                .content(StringUtils.join(content,"\n"))
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
}
