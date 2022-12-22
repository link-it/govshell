package it.govhub.govshell.proxy.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.commons.api.spec.ProfileApi;
import it.govhub.govregistry.commons.assemblers.ProfileAssembler;
import it.govhub.security.beans.GovhubPrincipal;

@RestController
public class ProfileController implements ProfileApi {
	
	@Autowired
	ProfileAssembler profileAssembler;

	@Override
	public ResponseEntity<Profile> profile() {
	
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		
		Profile ret = this.profileAssembler.toModel(principal.getUser());
		
		return ResponseEntity.ok(ret);
	}

}
