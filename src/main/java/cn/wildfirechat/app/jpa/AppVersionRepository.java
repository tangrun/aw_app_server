package cn.wildfirechat.app.jpa;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@CacheConfig(cacheNames = "AppVersionInfo")
@Repository
public interface AppVersionRepository extends CrudRepository<AppVersionEntry, Long> {

    @Query(value = "select * from app_version where user_id = ?1 and id < ?2 order by id desc limit ?3", nativeQuery = true)
    List<FavoriteItem> loadFeedBack(String userId, long startId, int count);

    @Cacheable(key = "#id",unless = "#result == null")
    AppVersionEntry findById(long id);
}
