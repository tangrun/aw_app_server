package cn.wildfirechat.app.pojo;

import lombok.Data;

@Data
public class FriendExtraInfo {


    // 对方给我设置的
    // 是下面我自己的设置时 同时设置到对方的好友信息上  下面自己设置用于自己页面上显示判断 设置到对方上的用作功能使用时是否可用的判断
    Boolean noScreenshot; // 对方禁止我截屏
    Boolean noSendUrgentMsg; // 对方禁止我给他发着急令

    // 我自己的设置
    Boolean disableScreenshot;// 设置对方禁止截屏
    Boolean disableFileForward; //设置对方禁止文件转发
    Boolean enableLeaveChatClearList;//设置退出清空消息
    Boolean disableSendUrgentMsg;//设置对方禁止发着急令

    public static FriendExtraInfo defaultExtra() {
        FriendExtraInfo info = new FriendExtraInfo();
        // 禁止文件转发
        info.setDisableFileForward(false);
        // 禁止截屏
        info.setDisableScreenshot(false);
        info.setNoScreenshot(false);
        // 退出清空消息
        info.setEnableLeaveChatClearList(false);
        // 禁止发送着急令
        info.setDisableSendUrgentMsg(false);
        info.setNoSendUrgentMsg(false);
        return info;
    }

}
