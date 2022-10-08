package cn.wildfirechat.app;

import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.jpa.UserEntity;
import cn.wildfirechat.app.jpa.UserEntityRepository;
import cn.wildfirechat.pojos.InputOutputUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 临时维护
 */
@RequestMapping("iXz621Hscgebgas1WUcZ6aqzRPZeS")
@RestController
public class MaintainController {

    @Autowired
    AdminService adminService;

    @Autowired
    UserEntityRepository userRepository;

    /**
     * 用户密码表 userid为空的
     *
     * @return
     */
    @GetMapping("user_pwd_userid_22_02_18")
    public String user_pwd_userid_22_02_18() {
        List<UserEntity> all = userRepository.findAll(new Specification<UserEntity>() {
            @Override
            public Predicate toPredicate(Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                predicate.getExpressions().add(
                        criteriaBuilder.and(criteriaBuilder.isNull(root.get("userId")), criteriaBuilder.isNotNull(root.get("mobile")))
                );

                return predicate;
            }
        });
        List<UserEntity> collect = all.stream().map(userPwdEntry -> {
            InputOutputUserInfo user = adminService.getUserByMobile(userPwdEntry.getMobile());
            if (user != null) {
                userPwdEntry.setUserId(user.getUserId());
                return userPwdEntry;
            }
            return null;
        }).filter(userPwdEntry -> {
            return userPwdEntry != null;
        }).collect(Collectors.toList());
        userRepository.saveAll(collect);
        return "ok";
    }

    /**
     * 密码登录遗留问题 需要记录下手机号做登录验证
     *
     * @return
     */
    @GetMapping("mobile22_02_09")
    public String mobile22_02_09() {

        List<UserEntity> all = userRepository.findAll(new Specification<UserEntity>() {
            @Override
            public Predicate toPredicate(Root<UserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                predicate.getExpressions().add(
                        criteriaBuilder.or(criteriaBuilder.isNull(root.get("mobile")), criteriaBuilder.equal(root.get("mobile"), ""))
                );

                return predicate;
            }
        });
        for (UserEntity entry : all) {
            InputOutputUserInfo user = adminService.getUserById(entry.getUserId());
            entry.setMobile(user.getMobile());
        }
        userRepository.saveAll(all);

        return "ok";
    }

}
