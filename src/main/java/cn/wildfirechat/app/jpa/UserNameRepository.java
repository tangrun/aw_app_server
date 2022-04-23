package cn.wildfirechat.app.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserNameRepository extends CrudRepository<UserNameEntry, Long> {}
