package cn.wildfirechat.app.admin;

public interface MessageContentType {

    int Type_Normal_Text = 1;//

    int Type_Tip_Notification = 90;//聊天界面 灰色提示
    int Type_Refresh_Friend_List = 1001;// 通知客户端刷新好友列表
    int Type_Work_Report_Send_To = 1010;//

    int Type_Schedule_share = 1011;//日程分享消息
    int Type_Schedule_receive = 1012;//日程待接收消息
    int Type_Group_Solitaire = 1020;// 接龙

}
