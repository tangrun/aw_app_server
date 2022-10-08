package cn.wildfirechat.app.work.schedule.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ScheduleQueryRequest {
    @NotNull
    private String date;
    private String userId;

}

