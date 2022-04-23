package cn.wildfirechat.app.group.repository;

import cn.wildfirechat.app.group.entity.SolitaireItemEntry;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolitaireTempRepository extends JpaRepositoryImplementation<SolitaireItemEntry, Long> {

    List<SolitaireItemEntry> findAllBySolitaireIdOrderById(Long solitaireId);

}
