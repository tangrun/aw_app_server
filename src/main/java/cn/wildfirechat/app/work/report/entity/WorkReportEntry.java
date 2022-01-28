package cn.wildfirechat.app.work.report.entity;

import cn.wildfirechat.app.work.report.enums.WorkReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "work_report",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
public class WorkReportEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id",nullable = false)
    public String userId;

    @Column(name = "job_content",nullable = false)
    public String jobContent;

    @Column(name = "job_plan")
    public String jobPlan;

    @Column(name = "other_matters")
    public String otherMatters;

    public String attachment;

    /**
     * 0、汇报；1、日报；2、周报；3、月报；4、年报
     */
    @Column(name = "type",nullable = false)
    @Enumerated(EnumType.STRING)
    public WorkReportType type;

    @CreationTimestamp
    @Column(name = "create_time")
    private Date createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private Date updateTime;


}
