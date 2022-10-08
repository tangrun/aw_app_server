package cn.wildfirechat.app.work.schedule.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TodoQueryRequest {
    @NotNull
    private Integer isfinish;

    private Integer type;

}

