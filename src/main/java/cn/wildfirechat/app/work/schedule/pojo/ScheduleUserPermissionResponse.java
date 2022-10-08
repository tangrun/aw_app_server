package cn.wildfirechat.app.work.schedule.pojo;

import lombok.Data;

@Data
public class ScheduleUserPermissionResponse {
    private Boolean readable;
    private Boolean writeable;
}
