package cn.wildfirechat.app.common.repository;

import cn.wildfirechat.app.common.entity.UploadFile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadFileRepository extends JpaRepositoryImplementation<UploadFile,String> {

    @Query("select u.localPath from UploadFile u where  u.id = :id")
    String findLocalPathById(@Param("id") String id);

    UploadFile findByMd5(String md5);
}
