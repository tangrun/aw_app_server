package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource()
public interface AppVersionRepository extends CrudRepository<AppVersionEntry, Long> {

    @Query(value = "select * from app_version where user_id = ?1 and id < ?2 order by id desc limit ?3", nativeQuery = true)
    List<FavoriteItem> loadFeedBack(String userId, long startId, int count);
}
