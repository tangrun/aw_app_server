package cn.wildfirechat.app.admin;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.pojo.FriendExtraInfo;
import cn.wildfirechat.app.tools.Invoker;
import cn.wildfirechat.app.work.report.entity.WorkReportEntry;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.*;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.sdk.MessageAdmin;
import cn.wildfirechat.sdk.RelationAdmin;
import cn.wildfirechat.sdk.model.IMResult;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminService {


    public RestResult sendWorkReportMessage(String fromUser, String targetId, WorkReportEntry entry){
//        Conversation conversation = new Conversation();
//        conversation.setTarget(targetId);
//        conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
//        MessagePayload payload = new MessagePayload();
//        payload.setType(MessageContentType.Type_Tip_Notification);
//        payload.setPersistFlag(ProtoConstants.PersistFlag.Persist);
//        payload.setContent(content);
//        try {
//            IMResult<SendMessageResult> result = MessageAdmin.sendMessage(fromUser, conversation, payload);
//            return RestResult.result(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
//        }
        return null;
    }

    /**
     * 设置用户扩展信息
     * @param userId
     * @param targetId
     * @param invoker
     * @return
     */
    public RestResult setFriendExtra(String userId, String targetId, Invoker<FriendExtraInfo> invoker) {
        log.debug( "setUserExtra() called with: userId = [{}], targetId = [{}]",userId,targetId);
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
     * @param fromUser
     * @param targetUser
     * @param content
     * @return
     */
    public RestResult sendTipNotificationMessage(String fromUser, String targetUser, String content ){
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
