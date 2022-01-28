package cn.wildfirechat.app.work.report.repository;

import cn.wildfirechat.app.work.report.entity.WorkReportToEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource()
public interface WorkReportTempRepository extends JpaRepositoryImplementation<WorkReportToEntry, Long> {


    @Query("select u.reportId from  WorkReportToEntry u where u.targetId = :targetId")
    List<Long> findAllReportIdByTargetId(@Param("targetId") String targetId);

    void deleteAllByReportId(Long reportId);

    List<WorkReportToEntry> findAllByReportId(Long reportId);

}
