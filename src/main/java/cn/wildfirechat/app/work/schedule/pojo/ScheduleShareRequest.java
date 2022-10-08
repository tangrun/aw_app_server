package cn.wildfirechat.app.work.schedule.pojo;

import cn.wildfirechat.pojos.Conversation;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ScheduleShareRequest {

    /**
     * 日程： 当天的时间
     */
    @NotNull
    Long time;
    /**
     * id
     */
    @NotNull

    List<Long> ids;
    /**
     * 类别 0日程 1待办
     */
    @NotNull
    Integer type;

    /**
     * 分享人
     */
    @NotNull
    @Size(max = 9)
    List<Conversation> conversationList;


    String userId;
}
