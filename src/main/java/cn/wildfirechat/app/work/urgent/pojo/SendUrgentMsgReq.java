package cn.wildfirechat.app.work.urgent.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

@Data
public class SendUrgentMsgReq {

    @NotNull
    private String targetUserId;

    @NotNull
    private String content;

}
