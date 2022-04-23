package cn.wildfirechat.app.jpa;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ConferenceEntityRepository extends PagingAndSortingRepository<ConferenceEntity, String> {


}
