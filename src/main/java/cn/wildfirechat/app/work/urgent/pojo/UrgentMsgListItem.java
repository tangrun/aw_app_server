package cn.wildfirechat.app.work.urgent.pojo;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import java.util.Date;

@Data
public class UrgentMsgListItem {
    private Long id;

    private String sendUserId;

    private String targetUserId;

    private String content;

    private Date createTime;
}
