package cn.wildfirechat.app.work.schedule;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SchedulePermissionRepository extends JpaRepositoryImplementation<SchedulePermissionEntity, Long> {


    /**
     * 查询我的日程权限设置
     * @param userId
     * @return
     */
    @Query(value = "select * from t_schedule_permission where uid=?1 and fid=?2 LIMIT 1", nativeQuery = true)
    SchedulePermissionEntity findUserSet(String userId,String friendId);

    /**
     * 查询我的日程权限设置
     * @param userId
     * @return
     */
    @Query(value = "select * from t_schedule_permission where uid=?1", nativeQuery = true)
    List<SchedulePermissionEntity> findUserSet(String userId);

    /**
     * 查询 好友 对我的日程权限设置
     * @param fId
     * @return
     */
    @Query(value = "select * from t_schedule_permission where fid=?1", nativeQuery = true)
    List<SchedulePermissionEntity> findSetByFid(String fId);

    /**
     * 删除日程
     * @param id
     * @param type
     * @param repeat_time
     * @param time
     * @return
     */
    @Transactional
    @Modifying
    @Query(value = "CALL udp_delete_info_tips_by_id(?1,?2,?3,?4);", nativeQuery = true)
    int removeInfoTips(long id, int type, int repeat_time,String time);

    /**
     * 修改权限设置
     * @param id
     * @param uid
     * @param fid
     * @param look
     * @param edit
     * @return
     */
    @Transactional
    @Modifying
    @Query(value = "CALL udp_update_schedule_permission(?1,?2,?3,?4,?5)", nativeQuery = true)
    void modifyPermission(long id, String uid, String fid, int look, int edit);

}
