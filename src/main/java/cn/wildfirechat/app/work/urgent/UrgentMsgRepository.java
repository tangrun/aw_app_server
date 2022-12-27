package cn.wildfirechat.app.work.urgent;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrgentMsgRepository extends JpaRepositoryImplementation<UrgentMsgEntity,Long> {

    List<UrgentMsgEntity> findAllByTargetUserIdAndSendUserDeleteIsFalse(String targetUserId, Pageable pageable);

    List<UrgentMsgEntity> findAllByTargetUserIdAndTargetUserDeleteIsFalse(String targetUserId, Pageable pageable);
}
