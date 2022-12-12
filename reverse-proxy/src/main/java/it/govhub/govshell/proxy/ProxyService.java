package it.govhub.govshell.proxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import it.govhub.govregistry.api.entity.UserEntity;
import it.govhub.govregistry.api.exception.ResourceNotFoundException;
import it.govhub.govregistry.api.services.SecurityService;

@Service
public class ProxyService {
	
    @Value("${govshell.auth.header}")
    private String headerAuthentication;
    
    @Value("${govshell.proxy.trace.header-name}")
    private String traceHeaderName;
    
    @Value("${govshell.proxy.hostname}")
    private String proxyHostName;
    
    @Autowired
    private ApplicationRepository appRepo;
    
    private final static Logger logger =  LoggerFactory.getLogger(ProxyService.class);

    public ResponseEntity<String> processProxyRequest(String applicationId, String body,
                                                      HttpMethod method, HttpServletRequest request, HttpServletResponse response, String traceId) throws URISyntaxException {
    	
        ThreadContext.put(this.traceHeaderName, traceId);
        
        ApplicationEntity application = this.appRepo.findByApplicationId(applicationId)
        		.orElseThrow(ResourceNotFoundException::new);
        
        URI requestUri = new URI(request.getRequestURI());
        URI applicationUri = new URI(application.getDeployedUri());
        
        String requestPath = requestUri.getPath();
        
        String resourcePath = requestPath.substring(applicationId.length()+1);
        
        URI resolvedUri = UriComponentsBuilder.fromUri(applicationUri)
        	.path(resourcePath)
        	.query(request.getQueryString())
        	.build(true).toUri();
        
        // Aggiungo Headers
        
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        headers.set(this.traceHeaderName, traceId);
        headers.remove(HttpHeaders.ACCEPT_ENCODING);
        
        // Aggiungo header di autenticazione 
        UserEntity principal = SecurityService.getPrincipal();
        headers.set(this.headerAuthentication, principal.getPrincipal());
        
        // Aggiungo header X-Forwarded-For per notificare all'endpoint la presenza del proxy.

        headers.set("X-Forwarded-For", request.getRemoteAddr());
        
        // Questo fa modificare la generazione dei link hateoas, che tengono in considerazione il proxy
        headers.set("X-Forwarded-Host",  this.proxyHostName);
        
        // Questo fa modificare la generazione dei link hateoas, che tengono in considerazione il prefisso del path con cui è stato chiamato il proxy
        headers.set("X-Forwarded-Prefix",  "/"+applicationId);


        // TODO: la factory la si potrebbe invece reare una sola volta in fase di creazione del bean?
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        RestTemplate restTemplate = new RestTemplate(factory);
        try {

            ResponseEntity<String> serverResponse = restTemplate.exchange(resolvedUri, method, httpEntity, String.class);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.put(HttpHeaders.CONTENT_TYPE, serverResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
            logger.info(serverResponse.toString());
            return serverResponse;

        } catch (HttpStatusCodeException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }


    @Recover
    public ResponseEntity<String> recoverFromRestClientErrors(Exception e, String body,
                                                              HttpMethod method, HttpServletRequest request, HttpServletResponse response, String traceId) {
        logger.error("retry method for the following url " + request.getRequestURI() + " has failed" + e.getMessage());
        logger.error(e.getStackTrace().toString());
        throw new RuntimeException("There was an error trying to process you request. Please try again later");
    }
}