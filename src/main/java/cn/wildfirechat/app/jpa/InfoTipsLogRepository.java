package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RepositoryRestResource()
public interface InfoTipsLogRepository extends CrudRepository<InfoTipsLogEntry, Long> {

    @Query(value = "select * from info_tips_log where user_id = ?1 order by id desc limit ?2", nativeQuery = true)
    List<InfoTipsLogEntry> loadFav(String userId, int count);


    /**
     * 取消任务已完成设置
     * @param id
     * @param time
     * @return
     */
    @Transactional
    @Modifying
    @Query(value = "delete from `info_tips_log` where tips_id=?1 and finish_time=?2", nativeQuery = true)
    int setInfoTipsNotFinish(long id,String time);
}
