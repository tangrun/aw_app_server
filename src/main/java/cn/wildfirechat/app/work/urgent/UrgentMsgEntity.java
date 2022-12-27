package cn.wildfirechat.app.work.urgent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "t_urgent_msg",
        indexes = {
                @Index(name = "idx_send_user_id", columnList = "_send_user_id"),
                @Index(name = "idx_target_user_id", columnList = "_target_user_id")
        }
)
public class UrgentMsgEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "_id")
    private Long id;

    @Column(name = "_send_user_id")
    private String sendUserId;

    @Column(name = "_target_user_id")
    private String targetUserId;

    @Column(name = "_content")
    private String content;

    @Column(name = "_read")
    private Boolean read = false;

    @Column(name = "_delete_t")
    private Boolean targetUserDelete = false;

    @Column(name = "_delete_s")
    private Boolean sendUserDelete = false;

    @CreationTimestamp
    @Column(name = "_create_time")
    private Date createTime;
}
