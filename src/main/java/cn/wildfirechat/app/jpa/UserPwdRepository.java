package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;


@Repository
public interface UserPwdRepository extends JpaRepositoryImplementation<UserPwdEntry, Long> {


    UserPwdEntry findByUserId(String userId);

    UserPwdEntry findByMobile(String mobile);
}
