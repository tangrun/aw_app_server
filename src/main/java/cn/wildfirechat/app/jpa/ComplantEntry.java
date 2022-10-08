package cn.wildfirechat.app.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

/**
 *
 * 投诉举报
 *
 */
@Entity
@Table(name = "complant")
public class ComplantEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    /**
     * 发起投诉人
     */
    @Column(name = "user_id")
    public String user_id;

    /**
     * 0、私聊；1、群聊
     */
    @Column(name = "target_type")
    public int target_type;

    /**
     * 被投诉对象
     */
    @Column(name = "target_id")
    public String target_id;

    /**
     * 投诉类型
     * 0、色情；1、违法犯罪及违禁品；2、赌博；3、政治谣言；4、暴恐血腥；5、辱骂诅咒；6、其他违规内容
     * 10、金融诈骗（贷款/提额/代开/套现等）；11、网络兼职刷单诈骗；12、返利诈骗；13、网络交友诈骗
     * 14、虚假投资理财诈骗；15、赌博诈骗；16、仿冒他人诈骗；17、免费送诈骗；18、游戏相关诈骗（代练/充值等）；19、其他诈骗行为
     *
     * 31、此账号可能被盗用了；32、侵犯未成年人权益
     */
    @Column(name = "type")
    public int type;

    /**
     * 投诉内容
     */
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

    /**
     * 备注（处理情况）
     */
    @Column(name = "remark")
    @Nullable
    public String remark;

//    /**
//     * 0、发布不适当内容对我造成骚扰；1、存在欺诈骗钱行为；2、此账号可能被盗用了；3、侵犯未成年人权益
//     */
//    @Column(name = "type")
//    public int type;




}
