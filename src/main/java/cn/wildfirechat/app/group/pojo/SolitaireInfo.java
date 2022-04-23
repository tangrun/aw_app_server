package cn.wildfirechat.app.group.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SolitaireInfo {

    /**
     * 修改时 接龙带上
     */
    public Long id;

    /**
     * 创建时传 群id
     */
    public String groupId;


    /**
     * 创建时传 补充信息
     */
    public String supply;

    /**
     * 创建和修改时传 自己的接龙条目
     */
    public List<SolitaireItemInfo> items;

    /**
     * 创建时 模板内容
     */
    public String template;

    /**
     * 主题 创建时传
     */
    public String theme;

    /**
     * @ignore
     */
    public String userId;

    /**
     * @ignore
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public Date createTime;
}
