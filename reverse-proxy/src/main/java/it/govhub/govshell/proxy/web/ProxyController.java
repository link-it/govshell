package it.govhub.govshell.proxy.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.UUID;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import it.govhub.govshell.proxy.services.ProxyService;


@RestController
public class ProxyController {
	
    @Autowired
    ProxyService service;
    
    @RequestMapping("/{application_id}/**")
    public ResponseEntity<String> sendRequestToSPM(
    				@Parameter(name = "application_id", required = true) @PathVariable("application_id") String applicationId, 
    				@RequestBody(required = false) String body,
    				HttpMethod method, 
    				HttpServletRequest request, 
    				HttpServletResponse response) 
            throws URISyntaxException {
    	
        return service.processProxyRequest(applicationId, body,method,request,response,UUID.randomUUID().toString());
    }
    
    @RequestMapping("/v1/{application_id}/**")
    public ResponseEntity<String> proxyMultipart(
    				@Parameter(name = "application_id", required = true) @PathVariable("application_id") String applicationId, 
    				HttpServletRequest request )
            throws URISyntaxException, IOException {
    	
    	ServletInputStream inStream = request.getInputStream();
    	
    	
    	HttpRequest newRequest = HttpRequest.newBuilder()
    			  .uri(new URI("https://postman-echo.com/post"))
    			  .headers("Content-Type", "text/plain;charset=UTF-8")
    			  .POST(HttpRequest.BodyPublishers.ofInputStream(() -> inStream))
    			  .build();
    	//Resource resource = ...
    	
    //	FormPartEvent a = null;
    	
    	return null;
	/*	Mono<String> result = webClient
		    .post()
		    .uri("https://example.com")
		    .body(Flux.concat(
		            FormPartEvent.create("field", "field value"),
		            FilePartEvent.create("file", resource)
		    ), PartEvent.class)
		    .retrieve()
		    .bodyToMono(String.class);*/
    	
    /*	WebClient client = WebClient.create();
    					return null;*/
    }
    

}