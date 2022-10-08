package cn.wildfirechat.app.admin;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.UserService;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("b32cd55b")
public class AdminController {

    @Autowired
    UserService userService;

    @PostMapping("32eecb72")
    @Validated
    public RestResult<Void> setUserState( String userId, String mobile, @NotNull Integer state, String remark) {
        if (StringUtils.isBlank(userId) && StringUtils.isBlank(mobile))
            return RestResult.error("userId mobile必须有一个不为空");
        return userService.setUserState(userId,mobile, state, remark);
    }

}
