package cn.wildfirechat.app.group.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "group_solitaire"
)
public class SolitaireEntry {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "gid")
    public String groupId;

    @Column(name = "message_id")
    private Long messageUid;

    @Column(name = "mid")
    public String userId;

    @Column(name = "theme")
    public String theme;

    @Column(name = "supply")
    public String supply;

    @CreationTimestamp
    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "template")
    public String template;


}
