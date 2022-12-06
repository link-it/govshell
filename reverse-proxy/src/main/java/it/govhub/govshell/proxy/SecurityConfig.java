package it.govhub.govshell.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import it.govhub.govregistry.api.security.GovhubUserDetailService;



/**
 * Configurazione della sicurezza
 * 
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig{
	
    @Value("${auth.header}")
    String headerAuthentication;

    // TODO: Questo va nella libreria comune
	public static final String JSESSIONID_NAME = "GOVHUB-JSESSIONID";

	@Autowired
	public GovhubUserDetailService userDetailService;

	@Bean
	public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
		applyAuthRules(http)
			.csrf().disable()																												// Disabilita csrf perchè il cookie di sessione viene rilasciato con SameSite: strict
		.formLogin()
			.loginProcessingUrl("/do-login")
			.failureUrl("/failure")
			.permitAll();
		// TODO: Questa parte sarà comune a tutte le applicazioni, rifattorizzare insieme alla libreria di autorizzazione.
		/*.exceptionHandling()
			.accessDeniedHandler(this.accessDeniedHandler())																		// Gestisci accessDenied in modo da restituire un problem ben formato
			.authenticationEntryPoint(new ProblemHttp403ForbiddenEntryPoint(this.jsonMapper));*/
	
		return http.build();
	}
	
	// TODO: Questo serve? boh
	/*@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName(JSESSIONID_NAME); 
		serializer.setCookiePath("/"); 
		serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$"); 
		return serializer;
	}*/
	
	
	public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler {
	    @Override
	    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
	            response.setStatus(HttpServletResponse.SC_OK);
	    }
	}
	
	
	private HttpSecurity applyAuthRules(HttpSecurity http) throws Exception {
		
		http
		.authorizeRequests()
		.anyRequest().permitAll();
		
		return http;
	}
	
}
