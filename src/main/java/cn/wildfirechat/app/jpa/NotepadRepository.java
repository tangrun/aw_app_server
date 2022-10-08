package cn.wildfirechat.app.jpa;

import cn.wildfirechat.app.work.report.entity.WorkReportEntry;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface NotepadRepository extends CrudRepository<NotepadEntry, Long> {

    /**
     * 获取所有
     * @param userId
     * @param startId
     * @param count
     * @return
     */
    @Query(value = "select * from notepad where uid = ?1 and id < ?2 order by is_top desc,create_time desc limit ?3", nativeQuery = true)
    List<NotepadEntry> loadNotepad(String userId, long startId, int count);

    /**
     * 获取指定类型
     * @param userId
     * @param startId
     * @param count
     * @param type
     * @return
     */
    @Query(value = "select * from notepad where uid = ?1 and id < ?2 and type =?4 order by is_top desc,create_time desc limit ?3", nativeQuery = true)
    List<NotepadEntry> loadNotepad(String userId, long startId, int count,int type);

    /**
     * 获取分类统计
     * @param userId
     * @return
     */
    @Query(value = "select type,CASE type WHEN 0 THEN '日常' WHEN 1 THEN '学习' WHEN 2 THEN '工作' WHEN 3 THEN '购物' WHEN 4 THEN '旅游' WHEN 5 THEN '娱乐' WHEN 6 THEN '交通' WHEN 7 THEN '医疗' ELSE '其它' END typeCN,COUNT(1) total from notepad where uid = ?1 GROUP BY type", nativeQuery = true)
    List<Map<String,Object>> loadNotepadTotal(String userId);




    @Query("select u from NotepadEntry u where "
            + "(u.uid = :userId)"
            + "order by u.isTop desc,u.create_time desc "
    )
    List<NotepadEntry> getNotepadAllList(@Param("userId") String userId, Pageable pageable);

    @Query("select u from NotepadEntry u where "
            + "(u.uid = :userId)"
            + "and (u.type = :type)"
            + "order by u.isTop desc,u.create_time desc "
    )
    List<NotepadEntry> getNotepadListByType(@Param("userId") String userId, @Param("type") int type, Pageable pageable);



    /**
     * 记事已 设置置顶
     * @param id
     * @param type
     * @return
     */
    @Transactional
    @Modifying
    @Query(value = "update notepad set is_top=?2 where id=?1", nativeQuery = true)
    int setNotepadTop(long id,int type);

}
