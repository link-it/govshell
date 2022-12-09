package it.govhub.govshell.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.api.assemblers.UserAssembler;
import it.govhub.govregistry.api.entity.UserEntity;
import it.govhub.govregistry.api.exception.UnreachableException;
import it.govhub.govregistry.api.services.SecurityService;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	private ObjectMapper jsonMapper;
	
	@Autowired
	private UserAssembler userAssembler;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.OK.value());
		
		ServletOutputStream outputStream = null;
		try{
			outputStream = response.getOutputStream();
			
	        UserEntity principal = SecurityService.getPrincipal();
	        
			this.jsonMapper.writeValue(outputStream, this.userAssembler.toProfileModel(principal));
			
			outputStream.flush();
		}catch(Exception e) {
			throw new UnreachableException();
		}
	}


}
