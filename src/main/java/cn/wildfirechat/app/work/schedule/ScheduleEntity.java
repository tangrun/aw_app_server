package cn.wildfirechat.app.work.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

/**
 * 自己新增
 * 日程
 */
@Data
@Entity
@Table(name = "info_tips")
public class ScheduleEntity {



    public static String[] Tag = new String[]{
            "工作", "生活", "学习", "运动"
    };
    public static String[] Repeat = new String[]{
            "不重复", "每天", "每周", "每月", "每年"//, "工作日"
    };
    public static String[] Tip = new String[]{
            "不提醒", "准点提醒", "提前10分钟", "提前30分钟", "提前1小时", "提前2小时", "提前1天", "提前3天"
    };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "user_id")
    public String user_id;

    @Column(name = "title")
    public String title;

    @Column(name = "tips_start_datetime")
    @Nullable
    public String tips_start_datetime;

    @Column(name = "tips_end_datetime")
    @Nullable
    public String tips_end_datetime;

    @Column(name = "is_full_day")
    public int is_full_day;

    @Column(name = "repeat_setting")
    public int repeat_setting;

    @Column(name = "repeat_time")
    public int repeat_time;

    @Column(name = "type")
    public int type;;

    @Column(name = "grade")
    public int grade;

    @Column(name = "tag")
    public String tag;

    @Column(name = "is_open")
    public int is_open;

    @Column(name = "address")
    public String address;

    @DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Column(name = "create_time")
    public Date create_time;

    @Column(name = "remark")
    public String remark;

    @Column(name = "pic1")
    public String pic1;

    @Column(name = "pic2")
    public String pic2;

    @Column(name = "pic3")
    public String pic3;


    @DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @Column(name = "end_time")
    @Nullable
    public Date end_time;

    @Column(name = "is_finish")
    public int is_finish;

    @Column(name = "fork_id")
    public Long forkId;

    @Column(name = "operator_id")
    private String operatorId;
}
