package cn.wildfirechat.app.pojo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 修改密码、设置密码相关
 */
@Data
public class ChangePasswordRequest {

    public interface Set {
    }

    public interface Reset {
    }

    public interface Forget {
    }

    @NotBlank(groups = {Forget.class})
    private String code;
    @NotBlank(groups = {Forget.class})
    private String mobile;
    @NotBlank(groups = {Reset.class})
    private String oldPwd;
    @NotBlank
    private String newPwd;

}
