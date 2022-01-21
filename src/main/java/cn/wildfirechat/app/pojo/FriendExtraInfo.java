package cn.wildfirechat.app.pojo;

import lombok.Data;

@Data
public class FriendExtraInfo {


    // 对方给我设置的
    Boolean noScreenshot; // 对方禁止我截屏

    // 我自己的设置
    Boolean disableScreenshot;// 设置对方禁止截屏
    Boolean disableFileForward; //设置对方禁止文件转发
    Boolean enableLeaveChatClearList;//设置退出清空消息

    public static FriendExtraInfo defaultExtra() {
        FriendExtraInfo info = new FriendExtraInfo();

        info.setDisableFileForward(false);

        info.setDisableScreenshot(false);
        info.setNoScreenshot(false);

        info.setEnableLeaveChatClearList(false);

        return info;
    }

}
