package cn.wildfirechat.app.group.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class SolitaireItemInfo {

    /**
     * 修改时带上这个id
     */
    public Long solitaireItemId;

    /**
     * 内容
     */
    public String content;

    /**
     * @ignore
     */
    public String userId;

    /**
     * @ignore
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date createTime;

    /**
     * @ignore
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date updateTime;
}
