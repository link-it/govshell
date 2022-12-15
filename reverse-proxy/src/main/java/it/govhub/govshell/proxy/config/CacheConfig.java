package it.govhub.govshell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableCaching
public class CacheConfig {
	
	public static final String APPLICATIONS = "applications";
	
	private Logger logger = LoggerFactory.getLogger(CacheConfig.class);
	
	@CacheEvict(value = APPLICATIONS, allEntries = true)
	@Scheduled(fixedRateString = "${caching.govhub.applications.TTL}")
	public void emptyApplicationsCache() {
		logger.info("CLEARING " + APPLICATIONS + " CACHE.");
	}
	

}
