package it.govhub.govshell.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import it.govhub.govregistry.api.config.SharedConfiguration;

/**
 * Applicazione Spring di GovShell.
 * 
 * Le entità e i repository vanno importati a mano per via della Import,
 * dove vengono specificati a loro volta i packages dei repository e delle entità.
 *
 */

@SpringBootApplication
@Import(SharedConfiguration.class)
@EnableJpaRepositories("it.govhub.govshell.proxy")
@EntityScan("it.govhub.govshell.proxy")
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    
}