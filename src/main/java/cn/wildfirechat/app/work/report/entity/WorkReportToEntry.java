package cn.wildfirechat.app.work.report.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_report_temp")
public class WorkReportToEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "report_id")
    public Long reportId;

    @Column(name = "target_id")
    public String targetId;

    @Column(name = "target_name")
    public String targetName;
}
