package it.govhub.govshell.proxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import it.govhub.govregistry.api.services.SecurityService;

@Service
public class ProxyService {
	
    @Value("${govshell.auth.header}")
    private String headerAuthentication;
	
    String domain = "localhost";
    Integer port = 10001;
    
    private final static Logger logger =  LoggerFactory.getLogger(ProxyService.class);

    /*@Retryable(exclude = {
            HttpStatusCodeException.class}, include = Exception.class, backoff = @Backoff(delay = 5000, multiplier = 4.0), maxAttempts = 4)*/
    public ResponseEntity<String> processProxyRequest(String body,
                                                      HttpMethod method, HttpServletRequest request, HttpServletResponse response, String traceId) throws URISyntaxException {
        ThreadContext.put("traceId", traceId);
        String requestUrl = request.getRequestURI();

        //log if required in this line
        URI uri = new URI("http", null, domain, port, null, null, null);

        // replacing context path form urI to match actual gateway URI
        uri = UriComponentsBuilder.fromUri(uri)
                .path(requestUrl)
                .query(request.getQueryString())
                .build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        headers.set("TRACE", traceId);
        headers.remove(HttpHeaders.ACCEPT_ENCODING);
        
        // Aggiungo header di autenticazione 
        UserEntity principal = SecurityService.getPrincipal();
        headers.set(this.headerAuthentication, principal.getPrincipal());
        
        // Aggiungo header X-Forwarded-For per notificare all'endpoint la presenza del proxy.

        headers.set("X-Forwarded-For", request.getRemoteAddr());
        
        // Questo fa modificare la generazione dei link hateoas, che tengono in considerazione il proxy
        headers.set("X-Forwarded-Host",  request.getLocalAddr()+":"+request.getLocalPort());


        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        RestTemplate restTemplate = new RestTemplate(factory);
        try {

            ResponseEntity<String> serverResponse = restTemplate.exchange(uri, method, httpEntity, String.class);
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