package cn.wildfirechat.app.jpa;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserPrivateConferenceIdRepository extends PagingAndSortingRepository<UserPrivateConferenceId, String> {


}
