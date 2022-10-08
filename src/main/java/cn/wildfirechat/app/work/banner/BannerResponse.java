package cn.wildfirechat.app.work.banner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse {

    private Long id;

    private String title;

    private String desc;

    private String url;

    private String imgUrl;
}
