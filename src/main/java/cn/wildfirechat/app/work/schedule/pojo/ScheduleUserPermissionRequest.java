package cn.wildfirechat.app.work.schedule.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ScheduleUserPermissionRequest {
    @NotNull
    String userId;
}
