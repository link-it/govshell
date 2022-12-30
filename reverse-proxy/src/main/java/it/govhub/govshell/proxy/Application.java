package it.govhub.govshell.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import it.govhub.govregistry.commons.config.CommonsExportedBeans;
import it.govhub.govregistry.commons.config.TimeZoneConfigurer;
import it.govhub.govregistry.commons.exception.handlers.RequestRejectedExceptionHandler;
import it.govhub.security.config.SecurityExportedBeans;



/**
 * Applicazione Spring di GovShell.
 * 
 * Le entità e i repository devono essere scansionati a mano per via della Import,
 * dove vengono specificati a loro volta i packages dei repository e delle entità.
 *
 * Escludiamo anche ErrorMvcAutoConfiguration in modo da disabilitare la pagina /error	
 */

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
@Import({CommonsExportedBeans.class, TimeZoneConfigurer.class, SecurityExportedBeans.class} )
@EnableJpaRepositories("it.govhub.govshell.proxy.repository")
@EntityScan("it.govhub.govshell.proxy.entities")
@ComponentScan( {"it.govhub.govregistry.readops.api.assemblers", "it.govhub.govshell.proxy"})
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
	/**
	 * Questo Bean Restituisce un Problem quando spring-security rifiuta una
	 * richiesta perchè ritenuta ad esempio non sicura.
	 */
	@Bean
	public RequestRejectedHandler requestRejectedHandler() {
	   return new RequestRejectedExceptionHandler();
	}
	
    
    
}