package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface ShiroSessionRepository extends JpaRepositoryImplementation<ShiroSession, String> {
}
