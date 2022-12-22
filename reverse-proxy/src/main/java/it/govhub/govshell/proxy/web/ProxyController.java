package it.govhub.govshell.proxy.web;

import java.net.URISyntaxException;
import java.util.UUID;

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
    
    /*@Autowired
    UserAssembler userAssembler;*/
    
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
    

}