package cn.wildfirechat.app.work.report.repository;

import cn.wildfirechat.app.work.report.entity.WorkReportEntry;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

}
