package it.govhub.govshell.proxy.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.ApplicationEntity;

public interface ApplicationRepository extends JpaRepositoryImplementation<ApplicationEntity, Long>{
	
	@Cacheable("applications")
	public Optional<ApplicationEntity> findByApplicationId(String applicationId);
}
