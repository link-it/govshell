package it.govhub.govshell.proxy.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.readops.api.assemblers.ProfileAssembler;
import it.govhub.govregistry.readops.api.spec.ProfileApi;
import it.govhub.security.beans.GovhubPrincipal;

@RestController
public class GovshellProfileController implements ProfileApi {
	
	@Autowired
	ProfileAssembler profileAssembler;

	@Override
	public ResponseEntity<Profile> profile() {
	
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		
		Profile ret = this.profileAssembler.toModel(principal.getUser());
		
		ret.setAuthorizations(null);
		
		return ResponseEntity.ok(ret);
	}

}
