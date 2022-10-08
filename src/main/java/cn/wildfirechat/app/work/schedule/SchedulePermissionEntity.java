package cn.wildfirechat.app.work.schedule;

import lombok.Data;
import javax.persistence.*;

/**
 * 对好友日程权限设置
 */
@Data
@Entity
@Table(name = "t_schedule_permission")
public class SchedulePermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "uid")
    public String uid;

    @Column(name = "fid")
    public String fid;

    /**
     * 读权限 0、对TA隐藏；1、对TA公开时间段；2、对TA公开日程详情
     */
    @Column(name = "look")
    public int look;

    /**
     * 写权限 0、无权限；1、可写入
     */
    @Column(name = "edit")
    public int edit;

}
