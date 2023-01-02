package it.govhub.govshell.proxy.security;

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
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.security.AccessDeniedHandlerImpl;
import it.govhub.govregistry.commons.security.ProblemHttp403ForbiddenEntryPoint;
import it.govhub.security.services.GovhubUserDetailService;



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
	
	@Autowired
	private AccessDeniedHandlerImpl accessDeniedHandler;
	
	@Autowired
	private LoginSuccessHandler loginSuccessHandler;
	
	@Autowired
	private  LoginFailureHandler loginFailureHandler;
	
	@Autowired
	private ExpiredSessionHandler expiredSessionHandler;
	
	@Autowired
	protected GovhubUserDetailService userDetailService;

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
