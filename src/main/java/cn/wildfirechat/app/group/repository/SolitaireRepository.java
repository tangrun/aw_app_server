package cn.wildfirechat.app.group.repository;

import cn.wildfirechat.app.group.entity.SolitaireEntry;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

@Repository
public interface SolitaireRepository extends JpaRepositoryImplementation<SolitaireEntry, Long> {

}
