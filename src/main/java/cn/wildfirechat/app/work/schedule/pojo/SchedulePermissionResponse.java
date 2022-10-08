package cn.wildfirechat.app.work.schedule.pojo;

import cn.wildfirechat.app.work.schedule.SchedulePermissionEntity;
import lombok.Data;

import java.util.List;

@Data
public class SchedulePermissionResponse {

    private List<SchedulePermissionEntity> mySet;

    private List<SchedulePermissionEntity> userSet;
}
