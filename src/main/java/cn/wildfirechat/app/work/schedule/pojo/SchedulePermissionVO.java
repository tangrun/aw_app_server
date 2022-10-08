package cn.wildfirechat.app.work.schedule.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SchedulePermissionVO {
    @NotNull
    Integer read;
    @NotNull
    Integer write;
    List<String> readableUserIdList;
    List<String> writableUserIdList;
}
