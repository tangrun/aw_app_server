package cn.wildfirechat.app.work.urgent.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteUrgentMsgReq {
    @NotNull
    private Long id;
}
