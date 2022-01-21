package cn.wildfirechat.app.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * 自己新增
 * 软件版本
 */
@Entity
@Table(name = "app_version")
public class AppVersionEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "download_url")
    public String download_url;

    @Column(name = "os_type")
    public int os_type;

    @Column(name = "version")
    public String version;

    @Column(name = "version_number")
    public String version_number;

    @Column(name = "update_content")
    public String update_content;

    @DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @Column(name = "update_time")
    public Date update_time;

    @Column(name = "file_size")
    public String file_size;

    @Column(name = "force_update")
    public int force_update;

}
