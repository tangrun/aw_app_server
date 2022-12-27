package cn.wildfirechat.app.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ExtraDisableRequest {

    @NotNull
    String targetId;
    @NotNull
    Boolean disable;

}
