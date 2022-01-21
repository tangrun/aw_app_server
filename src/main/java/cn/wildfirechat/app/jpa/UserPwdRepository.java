package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface UserPwdRepository extends CrudRepository<UserPwdEntry, Long> {


    /**
     * 根据ID获取详情
     * @param userId
     * @return
     */
    @Query(value = "select * from t_user_pwd where user_id = ?1 limit 1", nativeQuery = true)
    UserPwdEntry getUserPwdByUserId(String userId);

}
