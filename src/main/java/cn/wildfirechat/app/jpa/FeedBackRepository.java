package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedBackRepository extends CrudRepository<FeedBackEntry, Long> {

    @Query(value = "select * from feedback where user_id = ?1 and id < ?2 order by id desc limit ?3", nativeQuery = true)
    List<FavoriteItem> loadFeedBack(String userId, long startId, int count);

    FeedBackEntry findByUserIdAndType(String userId,Integer type);
}
