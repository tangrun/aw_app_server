package cn.wildfirechat.app.work.countdown;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.common.consts.SessionAttributes;
import cn.wildfirechat.app.common.consts.Groups;
import cn.wildfirechat.app.work.countdown.vo.AddOrCreateCountDownRequest;
import cn.wildfirechat.app.work.countdown.vo.CountDownInfo;
import cn.wildfirechat.app.work.countdown.vo.DeleteCountDownRequest;
import cn.wildfirechat.app.work.countdown.vo.QueryListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("countdown")
public class CountDownController {

    @Autowired
    CountDownService countDownService;

    @PostMapping("top")
    @Validated
    public RestResult<CountDownInfo> top(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @NotNull Long id, @NotNull Boolean isTop
    ) {
        return countDownService.setTop(userId, id,isTop);
    }

    @PostMapping("create")
    public RestResult<CountDownInfo> create(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @Validated AddOrCreateCountDownRequest request
    ) {
        return countDownService.addOrCreateCountDown(userId, request);
    }

    @PostMapping("update")
    public RestResult<CountDownInfo> update(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @Validated(value = Groups.Update.class) AddOrCreateCountDownRequest request
    ) {
        return countDownService.addOrCreateCountDown(userId, request);
    }

    @PostMapping("delete")
    public RestResult<Void> delete(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @Validated DeleteCountDownRequest request
    ) {
        return countDownService.deleteCountDown(userId, request.getId());
    }

    @PostMapping("list")
    public RestResult<List<CountDownInfo>> list(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @Validated QueryListRequest request
    ) {
        request.setUserId(userId);
        return countDownService.query(request);
    }
}
