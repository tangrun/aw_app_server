package cn.wildfirechat.app.work.schedule;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepositoryImplementation<ScheduleEntity, Long> {

    @Query(value = "select * from info_tips where user_id = :userId and fork_id in (:ids)",nativeQuery = true)
    List<ScheduleEntity> findAllByUser_idAndForkIdIn(@Param("userId") String user_id, @Param("ids") List<Long> ids);

    @Query(value = "select * from info_tips where user_id = ?1 and id < ?2 order by id desc limit ?3", nativeQuery = true)
    List<ScheduleEntity> loadInfoTips(String userId, long startId, int count);

    /**
     * 根据实际查询
     * @param userId
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value = "CALL udp_get_info_tips_info_date(?1,?2,?3)", nativeQuery = true)
    List<ScheduleEntity> loadInfoTipsByDate(String userId, String startTime, String endTime);

    @Query(value = "CALL get_schedule_temp(?1,?2,?3,?4)", nativeQuery = true)
    List<ScheduleEntity> loadInfoTipsByDate(String userId, String startTime, String endTime,String operatorId);

    @Query(value = "select * from info_tips where user_id=?1 and type=?2 and is_finish=?3", nativeQuery = true)
    List<ScheduleEntity> findAllByUser_idAndTypeAndIs_finish(String userId,int type,int is_finish);

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
     * 记事已完成 设置
     * @param id
     * @param type
     * @return
     */
    @Transactional
    @Modifying
    @Query(value = "update info_tips set is_finish=?2 where id=?1", nativeQuery = true)
    int setNotepadFinish(long id,int type);

    /**
     * 根据ID获取详情
     * @param id
     * @return
     */
    @Query(value = "select * from info_tips where id = ?1", nativeQuery = true)
    ScheduleEntity getInfoTipsById(long id);

    /**
     * 修改记事
     * @param id
     * @param title
     * @param repeat_setting
     * @param repeat_time
     * @return
     */
    @Query(value = "CALL udp_update_info_tips_notepad_info(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13)", nativeQuery = true)
    ScheduleEntity modifyNotepad(long id, String title, int repeat_setting, int repeat_time, int grade, String tag, int type, int is_open, String address, String remark, String pic1, String pic2, String pic3);

    /**
     * 修改日程
     * @param id
     * @param title
     * @param repeat_setting
     * @param repeat_time
     * @return
     */
    @Query(value = "CALL udp_update_info_tips_customer_info(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19)", nativeQuery = true)
    ScheduleEntity modifyInfoTips(long id, String userId, String title, String tips_start_datetime, String tips_end_datetime, int is_full_day,
                                  int repeat_setting, int repeat_time, int grade, String tag, int type, int is_open, String address, String remark, String pic1, String pic2, String pic3, int edit_type, String time);

}
