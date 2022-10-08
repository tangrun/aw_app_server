package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplantRepository extends CrudRepository<ComplantEntry, Long> {

    @Query(value = "select * from complant where user_id = ?1 and id < ?2 order by id desc limit ?3", nativeQuery = true)
    List<ComplantEntry> loadComplant(String userId, long startId, int count);
}
