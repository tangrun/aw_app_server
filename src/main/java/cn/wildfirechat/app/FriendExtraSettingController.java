package cn.wildfirechat.app;

import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.common.consts.SessionAttributes;
import cn.wildfirechat.app.pojo.ExtraDisableRequest;
import cn.wildfirechat.app.pojo.FriendExtraInfo;
import cn.wildfirechat.app.tools.Invoker;
import cn.wildfirechat.app.tools.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("extra/friend")
public class FriendExtraSettingController {

    @Autowired
    private AdminService adminService;

    @PostMapping(value = "disableSendUrgentMsg")
    public RestResult<Void> disableSendUrgentMsg(@SessionAttribute(SessionAttributes.userId) String userId, @RequestBody @Validated ExtraDisableRequest request) {
        // 设置对方标记
        RestResult<Void> restResult = adminService.setFriendExtra(request.getTargetId(), userId, new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setNoSendUrgentMsg(request.getDisable());
            }
        });
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) {
            log.error("disableSendUrgentMsg setNoSendUrgentMsg {}", restResult);
        }
        // 通知刷新
        restResult = adminService.sendRefreshFriendListNotifyMessage(userId, request.getTargetId());
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) {
            log.error("disableSendUrgentMsg sendRefreshFriendListNotifyMessage {}", restResult);
        }
        //设置自己的标记
        restResult = adminService.setFriendExtra(userId, request.getTargetId(), new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setDisableSendUrgentMsg(request.getDisable());
            }
        });
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) {
            log.error("disableSendUrgentMsg setDisableSendUrgentMsg {}", restResult);
        }
        return RestResult.ok();
    }

    /**
     * 设置禁止对方截屏
     *
     * @param request
     * @return
     */
    @PostMapping(value = "disableScreenshot")
    public RestResult<Void> disableScreenshot(@SessionAttribute(SessionAttributes.userId) String userId, @RequestBody @Validated ExtraDisableRequest request) {
        // 设置对方标记
        RestResult<Void> restResult = adminService.setFriendExtra(request.getTargetId(), userId, new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setNoScreenshot(request.getDisable());
            }
        });
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) {
            log.error("disableScreenshot setNoScreenshot {}", restResult);
        }
        // 通知刷新
        restResult = adminService.sendRefreshFriendListNotifyMessage(userId, request.getTargetId());
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) {
            log.error("disableScreenshot sendRefreshFriendListNotifyMessage {}", restResult);
        }
        //设置自己的标记
        restResult = adminService.setFriendExtra(userId, request.getTargetId(), new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setDisableScreenshot(request.getDisable());
            }
        });
        if (restResult.getCode() != RestResult.RestCode.SUCCESS.code) {
            log.error("disableScreenshot setDisableScreenshot {}", restResult);
        }
        return RestResult.ok();
    }

    /**
     * 设置文件禁止转发
     *
     * @param request
     * @return
     */
    @PostMapping(value = "disableFileForward")
    public RestResult disableFileForward(@SessionAttribute(SessionAttributes.userId) String userId, @RequestBody @Validated ExtraDisableRequest request) {
        return adminService.setFriendExtra(userId, request.getTargetId(), new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setDisableFileForward(request.getDisable());
            }
        });
    }


    /**
     * 设置离开聊天清空消息记录
     *
     * @param request
     * @return
     */
    @PostMapping(value = "enableLeaveChatClearList")
    public RestResult enableLeaveChatClearList(@SessionAttribute(SessionAttributes.userId) String userId, @RequestBody @Validated ExtraDisableRequest request) {
        return adminService.setFriendExtra(userId, request.getTargetId(), new Invoker<FriendExtraInfo>() {
            @Override
            public void onInvoke(FriendExtraInfo target) {
                target.setEnableLeaveChatClearList(!request.getDisable());
            }
        });
    }

}
