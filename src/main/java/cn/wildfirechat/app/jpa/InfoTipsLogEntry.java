package cn.wildfirechat.app.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * 自己新增
 * 日程 日志
 */
@Entity
@Table(name = "info_tips_log")
public class InfoTipsLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "tips_id")
    public Long tips_id;

    @Column(name = "finish_time")
    public String finish_time;

    @Column(name = "remark")
    public String remark;

    @DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Column(name = "create_time")
    public Date create_time;

}
