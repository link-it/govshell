package it.govhub.govshell.proxy.web;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import it.govhub.govshell.proxy.services.ProxyService;


@RestController
public class ProxyController {
	
    @Autowired
    ProxyService service;
    
    @RequestMapping("/{application_id}/**")
    public ResponseEntity<Resource> proxyMultipart(
    				@Parameter(name = "application_id", required = true) @PathVariable("application_id") String applicationId, 
    				HttpServletRequest request )
            throws URISyntaxException, IOException, InterruptedException {
    	
    	return this.service.processProxyRequest(applicationId, request);
       
    }

}