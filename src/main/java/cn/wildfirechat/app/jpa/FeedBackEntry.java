package cn.wildfirechat.app.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

/**
 * 自己新增
 * 意见反馈
 */
@Entity
@Table(name = "feedback")
public class FeedBackEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "user_id")
    public String user_id;

    @Column(name = "content")
    public String content;

    @Column(name = "attach_file")
    @Nullable
    public String attach_file;

    @Column(name = "attach_file1")
    @Nullable
    public String attach_file1;

    @Column(name = "attach_file2")
    @Nullable
    public String attach_file2;

    @Column(name = "attach_file3")
    @Nullable
    public String attach_file3;

    @Column(name = "attach_file4")
    @Nullable
    public String attach_file4;

    @Column(name = "attach_file5")
    @Nullable
    public String attach_file5;

    @DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @Column(name = "create_time")
    public Date create_time;

    @Column(name = "remark")
    @Nullable
    public String remark;

    /**
     * 0、意见反馈；1、账号注销
     */
    @Column(name = "type")
    public int type;

}
