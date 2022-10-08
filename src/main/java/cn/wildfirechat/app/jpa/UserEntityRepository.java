package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;


@Repository
public interface UserEntityRepository extends JpaRepositoryImplementation<UserEntity, Long> {

    UserEntity findFirstByUserId(String userId);

    UserEntity findFirstByMobile(String mobile);

    UserEntity findFirstByWechatUnionid(String wxCode);

    UserEntity findFirstByQqOpenid(String qqCode);

}
