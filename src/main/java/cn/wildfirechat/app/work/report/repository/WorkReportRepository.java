package cn.wildfirechat.app.work.report.repository;

import cn.wildfirechat.app.work.report.entity.WorkReportEntry;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface WorkReportRepository extends JpaRepositoryImplementation<WorkReportEntry, Long> {

    @Query("select u from WorkReportEntry u where "
            + "(:userId is null or u.userId = :userId)"
            + "and (coalesce(:ids, null) is null or u.id in (:ids))"
            + "and (:type is null or u.type = :type)"
            + "and (:beginTime is null or u.updateTime >= :beginTime)"
            + "and (:endTime is null or u.updateTime <= :endTime)"
            + "order by u.updateTime desc "
    )
    List<WorkReportEntry> getReportList(@Param("userId") String userId, @Param("ids") List<Long> ids, @Param("type") WorkReportType type, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime, Pageable pageable);

    @Query("select u from WorkReportEntry u,WorkReportToEntry t where "
            + "t.reportId = u.id "
            + "and (:userIds is null or u.userId in (:userIds)) "
            + "and (coalesce(:reportUserIds, null) is null or t.targetId in (:reportUserIds)) "
            + "and (:type is null or u.type = :type) "
            + "and (:beginTime is null or u.updateTime >= :beginTime) "
            + "and (:endTime is null or u.updateTime <= :endTime) "
            + "order by u.updateTime desc " )
    List<WorkReportEntry> getReportList2(@Param("userIds") List<String> userIds, @Param("reportUserIds") List<String> reportUserIds, @Param("type") WorkReportType type,@Param("beginTime") Date beginTime, @Param("endTime") Date endTime, Pageable pageable);

}
