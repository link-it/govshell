package it.govhub.govshell.proxy.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "govshell.ldap")
@Getter
@Setter
public class LdapConfiguration {

	private String userDnPatterns;
	private String userSearchFilter;
	private String userSearchBase;
	private String groupSearchFilter;
	private String groupSearchBase;
	private String serverUrl;
	private Integer serverPort;
	private String managerDn;
	private String managerPassword;
}
