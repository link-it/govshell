package it.govhub.govshell.proxy;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
	
	public static final String APPLICATIONS = "applications";

}
