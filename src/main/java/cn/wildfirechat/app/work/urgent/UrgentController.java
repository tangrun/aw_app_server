package cn.wildfirechat.app.work.urgent;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.common.consts.SessionAttributes;
import cn.wildfirechat.app.common.pojo.Page;
import cn.wildfirechat.app.work.urgent.pojo.DeleteUrgentMsgReq;
import cn.wildfirechat.app.work.urgent.pojo.SendUrgentMsgReq;
import cn.wildfirechat.app.work.urgent.pojo.UrgentMsgListItem;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.SendMessageResult;
import cn.wildfirechat.sdk.model.IMResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 着急令
 */
@RestController
@RequestMapping("urgent")
@Slf4j
public class UrgentController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UrgentMsgRepository urgentMsgRepository;

    @PostMapping("list/receive")
    public RestResult<List<UrgentMsgListItem>> getReceiveUrgentMsgList(@SessionAttribute(SessionAttributes.userId) String userId, Page page) {
        List<UrgentMsgEntity> list = urgentMsgRepository.findAllByTargetUserIdAndTargetUserDeleteIsFalse(userId, page.convert2PageRequest());
        return RestResult.ok(list.stream()
                        .map(this::convert)
                .collect(Collectors.toList()));
    }

    @PostMapping("list/send")
    public RestResult<List<UrgentMsgListItem>> getSendUrgentMsgList(@SessionAttribute(SessionAttributes.userId) String userId, Page page) {
        List<UrgentMsgEntity> list = urgentMsgRepository.findAllByTargetUserIdAndSendUserDeleteIsFalse(userId, page.convert2PageRequest());
        return RestResult.ok(list.stream()
                .map(this::convert)
                .collect(Collectors.toList()));
    }

    private UrgentMsgListItem convert(UrgentMsgEntity entity){
        UrgentMsgListItem urgentMsgListItem = new UrgentMsgListItem();
        BeanUtils.copyProperties(entity, urgentMsgListItem);
        return urgentMsgListItem;
    }

    @PostMapping("delete")
    public RestResult<Void> delete(@SessionAttribute(SessionAttributes.userId) String userId,
                                   @Validated DeleteUrgentMsgReq data) {
        Optional<UrgentMsgEntity> optional = urgentMsgRepository.findById(data.getId());
        if (!optional.isPresent()) {
            return RestResult.error("删除失败");
        }
        UrgentMsgEntity urgentMsgEntity = optional.get();
        if (Objects.equals(urgentMsgEntity.getSendUserId(), userId)) {
            urgentMsgEntity.setSendUserDelete(true);
        }
        if (Objects.equals(urgentMsgEntity.getTargetUserId(), userId)) {
            urgentMsgEntity.setTargetUserDelete(true);
        }

        if (urgentMsgEntity.getSendUserDelete() == Boolean.TRUE && urgentMsgEntity.getTargetUserDelete() == Boolean.TRUE) {
            urgentMsgRepository.delete(urgentMsgEntity);
        } else
            urgentMsgRepository.saveAndFlush(urgentMsgEntity);

        return RestResult.ok();
    }

    @PostMapping("send")
    public RestResult<Void> sendUrgentMsg(@SessionAttribute(SessionAttributes.userId) String userId,
                                          @Validated SendUrgentMsgReq data) {
        UrgentMsgEntity urgentMsgEntity = new UrgentMsgEntity()
                .setSendUserId(userId)
                .setTargetUserId(data.getTargetUserId())
                .setContent(data.getContent());

        urgentMsgRepository.saveAndFlush(urgentMsgEntity);

        IMResult<SendMessageResult> result = adminService.sendUrgentMessage(urgentMsgEntity);
        if (result.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            return RestResult.ok();
        }

        urgentMsgRepository.delete(urgentMsgEntity);
        return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
    }
}
