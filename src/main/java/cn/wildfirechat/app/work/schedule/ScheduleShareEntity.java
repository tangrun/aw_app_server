package cn.wildfirechat.app.work.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_schedule_share")
public class ScheduleShareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id")
    String userId;

    int type;

    String ids;

    @CreationTimestamp
    @Column(name = "create_time")
    Date createTime;

    /**
     * 日程为时间的那一天 待办为-1
     */
    @Column(name = "time")
    Long time;
}
