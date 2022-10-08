package cn.wildfirechat.app.work.countdown.vo;

import cn.wildfirechat.app.common.consts.Groups;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class AddOrCreateCountDownRequest {

    @NotNull(groups = Groups.Update.class)
    private Long id;

    @NotNull
    private Date date;

    @NotBlank
    private String title;

    @NotBlank
    private String type;

    @NotNull
    private Long remind;
}
