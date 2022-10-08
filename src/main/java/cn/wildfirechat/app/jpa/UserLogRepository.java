package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;


@Repository
public interface UserLogRepository extends JpaRepositoryImplementation<UserLogEntry, Long> {


    UserEntity findByUserId(String userId);

}
