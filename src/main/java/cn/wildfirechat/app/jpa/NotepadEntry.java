package cn.wildfirechat.app.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

/**
 * 记事本
 */
@Entity
@Table(name = "notepad")
public class NotepadEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "uid")
    public String uid;

    /**
     * 0、其它；1、日常；2、旅游；3、交通；4、医疗；5、购物；6、娱乐；7学习；8、工作
     */
    @Column(name = "type")
    public int type;

    /**
     * 是否置顶：1、置顶
     */
    @Column(name = "is_top")
    public int isTop;

    @Column(name = "title")
    @Nullable
    public String title;

    @Column(name = "content")
    public String content;

//    @Column(name = "attach_file")
//    @Nullable
//    public String attach_file;
//
//    @Column(name = "attach_file1")
//    @Nullable
//    public String attach_file1;
//
//    @Column(name = "attach_file2")
//    @Nullable
//    public String attach_file2;

    @DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @Column(name = "create_time")
    public Date create_time;


}
