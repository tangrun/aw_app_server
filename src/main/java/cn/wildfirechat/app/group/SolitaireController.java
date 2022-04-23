package cn.wildfirechat.app.group;

import cn.wildfirechat.app.group.pojo.SolitaireInfo;
import cn.wildfirechat.app.group.service.SolitaireService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 群接龙
 */
@Slf4j
@RestController
@RequestMapping("/group/solitaire")
public class SolitaireController {

    @Autowired
    private SolitaireService solitaireService;

    /**
     * 创建群接龙
     * @param request
     * @return
     */
    @PostMapping("create")
    public Object create(SolitaireInfo request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        return solitaireService.create(userId, request);
    }

    /**
     * 添加一条接龙
     * @param request
     * @return
     */
    @PostMapping("append")
    public Object append(SolitaireInfo request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return solitaireService.append(userId, request);
    }

    /**
     * 获取单个接龙信息
     *
     * @param id
     * @return
     */
    @PostMapping("getSolitaireInfo")
    public Object getSolitaireInfo(Long id) {
        return solitaireService.getSolitaireInfo(id);
    }

}
