package cn.wildfirechat.app.work.countdown;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountDownRepository extends JpaRepositoryImplementation<CountDownEntity,Long> {

    CountDownEntity findByUserIdAndId(String userId,Long id);

    List<CountDownEntity> findAllByUserIdAndType(String userId,String type);
}
