package it.govhub.govshell.proxy.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govshell.proxy.entities.ApplicationEntity;
import it.govhub.govshell.proxy.repository.ApplicationRepository;
import it.govhub.security.services.SecurityService;


@Service
public class ProxyService {
	
	final Logger logger = LoggerFactory.getLogger(ProxyService.class);
	
	final static Set<String> reservedHeaders = Set.of(HttpHeaders.HOST, HttpHeaders.CONNECTION);

	@Value("${govshell.auth.header}")
	String headerAuthentication;

	@Value("${govshell.proxy.trace.header-name}")
	String traceHeaderName;

	@Value("${govshell.proxy.forwarded-prefix}")
	String forwardedPrefix;
	
	@Autowired
	ApplicationRepository appRepo;
	
	TreeSet<String> responseBlackListHeaders;
	
	HttpClient client;
	
	public ProxyService(
			@Value("${govshell.proxy.headers.response.blacklist}")	List<String> blackListHeaders,
			@Value("${govshell.proxy.connection-timeout}")	Integer connectionTimeout) {
		
		this.responseBlackListHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		this.responseBlackListHeaders.addAll(blackListHeaders);
		
		this.client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(connectionTimeout))
				.build();
	}
	

	public ResponseEntity<Resource> processProxyRequest(String applicationId, HttpServletRequest request)
			throws URISyntaxException, IOException, InterruptedException {

		String traceId = UUID.randomUUID().toString();

		ThreadContext.put(this.traceHeaderName, traceId);

		ApplicationEntity application = this.appRepo.findByApplicationId(applicationId)
				.orElseThrow(ResourceNotFoundException::new);

		URI applicationUri = new URI(application.getDeployedUri());
		String requestPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String prefix = "/" + applicationId;
		String resourcePath = requestPath.substring(prefix.length());
		URI destUri = UriComponentsBuilder.fromUri(applicationUri).path(resourcePath).query(request.getQueryString())
				.build(true).toUri();

		logger.debug("Proxying request: {}\nApplicationId: {}\nApplicationURI: {}\nSourceRequestPath: {}\nDestUri: {}", traceId, applicationId,
				applicationUri, requestPath, destUri);

		ServletInputStream inStream = request.getInputStream();

		Builder builder = HttpRequest.newBuilder()
				.uri(destUri)
				.method(request.getMethod(),
						HttpRequest.BodyPublishers.ofInputStream(() -> inStream));

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			
			if (reservedHeaders.contains(headerName)) {
				continue;
			}

			try {
				builder.header(headerName, request.getHeader(headerName));
			} catch (IllegalArgumentException e) {
				logger.error("Header riservato {}", headerName);
			}
		}

		builder.header(this.traceHeaderName, traceId);
		builder.header("X-Forwarded-Prefix", this.forwardedPrefix + "/" + applicationId);

		// Aggiungo header di autenticazione
		UserEntity principal = SecurityService.getPrincipal();
		builder.header(this.headerAuthentication, principal.getPrincipal());

		HttpRequest newRequest = builder.build();

		HttpResponse<InputStream> response = this.client.send(newRequest, BodyHandlers.ofInputStream());

		logger.debug("Proxying request: {} - Got response from backend: {}", traceId, response.statusCode());

		java.net.http.HttpHeaders respHeaders = response.headers();

		HttpHeaders retHeaders = new HttpHeaders();
		
		respHeaders.map().forEach((key, values) -> {
			if (!this.responseBlackListHeaders.contains(key)) {
				retHeaders.addAll(key, values);
			}
		});

		
		InputStreamResource resourceStream = new InputStreamResource(response.body());

		logger.debug("Proxying request: {} - Returning response to the client.", traceId);
		ResponseEntity<Resource> ret = new ResponseEntity<>(resourceStream, retHeaders, HttpStatus.OK);

		return ret;
	}
}