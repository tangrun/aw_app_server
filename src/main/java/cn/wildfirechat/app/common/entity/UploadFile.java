package cn.wildfirechat.app.common.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class UploadFile {

    @Id
    private String id;

    @Column(name = "local_path")
    private String localPath;

    @Column(name = "local_name")
    private String localName;

    @Column(name = "origin_name")
    private String originName;

    private String user;

    private String md5;

    @Column(name = "mimetype")
    private String mimetype;

    private Long size;

    @Column(name = "create_time")
    @CreationTimestamp
    private Date createTime;

}
