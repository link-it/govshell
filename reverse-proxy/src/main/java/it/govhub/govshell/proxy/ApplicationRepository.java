package it.govhub.govshell.proxy;

import java.util.Optional;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface ApplicationRepository extends JpaRepositoryImplementation<ApplicationEntity, Long>{
	
	public Optional<ApplicationEntity> findByApplicationId(String applicationId);
}
