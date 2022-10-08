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
@Entity(name = "t_banner")
@Table(indexes = {
        @Index(name = "idx_type", columnList = "_type"),
})
public class BannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "_type",nullable = false)
    @Enumerated(EnumType.STRING)
    private BannerType type;

    @Column(name = "_title")
    private String title;

    @Column(name = "_desc")
    private String desc;

    @Column(name = "_url",nullable = false)
    private String url;

    @Column(name = "_img_url",nullable = false)
    private String imgUrl;

    @Column(name = "_enabled",nullable = false)
    private Boolean enabled;



}
