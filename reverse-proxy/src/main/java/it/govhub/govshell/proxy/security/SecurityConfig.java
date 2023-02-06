package it.govhub.govshell.proxy.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.security.AccessDeniedHandlerImpl;
import it.govhub.govregistry.commons.security.ProblemHttp403ForbiddenEntryPoint;



/**
 * Configurazione della sicurezza
 * 
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig{
	
	@Value("${server.servlet.session.cookie.name:GOVHUB-JSESSIONID}")
	private String sessionCookieName;
	
	@Value("${govshell.auth.max-sessions:10}")
	private Integer maxSessions;
	
    @Value("${govhub.csp.policy:default-src 'self'}")
    String cspPolicy;
    
    @Value("${govshell.auth.type:form}")
    String authType;
    
    @Autowired
    LdapConfiguration ldapConfiguration;
	
	@Autowired
	private AccessDeniedHandlerImpl accessDeniedHandler;
	
	@Autowired
	private LoginSuccessHandler loginSuccessHandler;
	
	@Autowired
	private  LoginFailureHandler loginFailureHandler;
	
	@Autowired
	private ExpiredSessionHandler expiredSessionHandler;
	
	@Autowired
	LdapUserDetailsContextMapper contextMapper;
	
	Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
	
	@Bean
	public SecurityFilterChain securityFilterChainDev(HttpSecurity http, ObjectMapper jsonMapper) throws Exception {
		applyAuthRules(http)
			.csrf().disable()																												// Disabilita csrf perchè il cookie di sessione viene rilasciato con SameSite: strict
		.formLogin()
			.loginProcessingUrl("/do-login")
			.successHandler(this.loginSuccessHandler)
			.failureHandler(this.loginFailureHandler)
			.permitAll()
		.and()
		.exceptionHandling()
		// Gestisci accessDenied in modo da restituire un problem ben formato TODO: Vedi se a govshell serve davero
		.accessDeniedHandler(this.accessDeniedHandler)																
		// Gestisci la mancata autenticazione con un problem ben formato
		.authenticationEntryPoint(new ProblemHttp403ForbiddenEntryPoint(jsonMapper))	
		.and()
		.logout()
			.logoutUrl("/logout")
			.deleteCookies(this.sessionCookieName)
			.invalidateHttpSession(true)
			.logoutSuccessHandler(new DefaultLogoutSuccessHandler())
		.and()
		.headers()
			.xssProtection()
            .and()
         // Politica di CSP più restrittiva. https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP
         // Anche le immagini dal gravatar
        .contentSecurityPolicy(this.cspPolicy);
		
	    http.sessionManagement()
	    	.maximumSessions(maxSessions)
	    	.expiredSessionStrategy(this.expiredSessionHandler);
	
		return http.build();
	}
	
	@Autowired
	private Environment env;
	
	
	  @Autowired
	  public void configure(AuthenticationManagerBuilder auth) throws Exception {
		  
		  if (authType.equals("ldap") ) {
			  logger.info("Configuring Ldap Authentication..");
			  
			  auth
		      .ldapAuthentication()
		        .userDnPatterns(this.ldapConfiguration.getUserDnPatterns())
  	            .userSearchFilter(this.ldapConfiguration.getUserSearchFilter())
		        .userSearchBase(this.ldapConfiguration.getUserSearchBase())
		        .groupSearchBase(this.ldapConfiguration.getGroupSearchBase())
		        .groupSearchFilter(this.ldapConfiguration.getGroupSearchFilter())
		        .userDetailsContextMapper(contextMapper)
		        .contextSource()
		          .url(this.ldapConfiguration.getServerUrl())	
		          .port(this.ldapConfiguration.getServerPort())
		          .managerDn(this.ldapConfiguration.getManagerDn())
		    	  .managerPassword(this.ldapConfiguration.getManagerPassword());
		  }
		  
	  }
		  
		// TODO: Questa roba  si può configurare anche nell'application.properties e forse sarebbe meglio?
		  /*  ldap.urls= ldap://ldap.forumsys.com:389/
				ldap.base.dn= dc=example,dc=com
				ldap.username= cn=read-only-admin,dc=example,dc=com
				ldap.password= password
				ldap.user.dn.pattern = uid={0}
			
				*/
	
	private HttpSecurity applyAuthRules(HttpSecurity http) throws Exception {
		http
		.authorizeRequests()
		.anyRequest().authenticated();
		return http;
	}
	

	/**
	 * Pubblica gli eventi di sessione sul WebApplicationContext radice.
	 * Consente nel nostro caso di contare il numero di sessioni attive per utente e limitarlo di conseguenza.
	 * 
	 */
	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
	    return new HttpSessionEventPublisher();
	}
	
	
	public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler {
	    @Override
	    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
	            response.setStatus(HttpServletResponse.SC_OK);
	    }
	}
	
}
