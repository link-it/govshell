package it.govhub.govshell.proxy;

import java.net.URISyntaxException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.govhub.govregistry.api.assemblers.UserAssembler;
import it.govhub.govregistry.api.beans.Problem;
import it.govhub.govregistry.api.beans.Profile;
import it.govhub.govregistry.api.security.GovhubPrincipal;

@RestController
public class ProxyController {
	
    @Autowired
    ProxyService service;
    
    @Autowired
    UserAssembler userAssembler;
    
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
    
    
    /**
     * GET /profile : Get the detail of the authenticated user.
     * Get the detail of the authenticated user.
     *
     * @return Successful operation. (status code 200)
     *         or Bad Request. (status code 400)
     *         or Required credentials missing. (status code 401)
     *         or No Authorizations for the requested operation. (status code 403)
     *         or Too many requests. (status code 429)
     *         or Service Unavailable. (status code 503)
     *         or Unexpected error. (status code 200)
     */
    @Operation(
        operationId = "profile",
        summary = "Get the detail of the authenticated user.",
        tags = { "system" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Profile.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Profile.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "401", description = "Required credentials missing.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "403", description = "No Authorizations for the requested operation.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "503", description = "Service Unavailable.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "200", description = "Unexpected error.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/profile",
        produces = { "application/json", "application/problem+json" }
    )
    ResponseEntity<Profile> profile() {
    		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
    		
    		Profile ret = this.userAssembler.toProfileModel(principal.getUser());
    		
    		return ResponseEntity.ok(ret);
    }
}