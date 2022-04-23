package cn.wildfirechat.app.admin.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TextCardInfo {

    private String title;
    private String content;
    private String icon;
    private String tag;

}
