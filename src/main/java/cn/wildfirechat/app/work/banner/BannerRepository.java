package cn.wildfirechat.app.work.banner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepositoryImplementation<BannerEntity,Long> {

    List<BannerEntity> getAllByEnabledAndType(Boolean enabled,BannerType type);

    List<BannerEntity> getAllByType(BannerType type);

}
