package it.govhub.govshell.proxy;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface ApplicationRepository extends JpaRepositoryImplementation<ApplicationEntity, Long>{
	
	@Cacheable("applications")
	public Optional<ApplicationEntity> findByApplicationId(String applicationId);
}
