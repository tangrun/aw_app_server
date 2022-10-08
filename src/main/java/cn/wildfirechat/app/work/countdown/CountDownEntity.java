package cn.wildfirechat.app.work.countdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "t_countdown")
@Table(indexes = {
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_user_id", columnList = "user_id"),
})
public class CountDownEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "title")
    private String title;

    @Column(name = "type")
    private String type;

    @CreationTimestamp
    @Column(name = "create_time")
    private Date createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "remind")
    private Long remind;

    @Column(name = "is_top")
    private Boolean top;
}
