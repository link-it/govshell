package it.govhub.govshell.proxy.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.commons.profile.api.beans.Profile;
import it.govhub.commons.profile.api.spec.ProfileApi;
import it.govhub.govregistry.api.assemblers.UserAssembler;
import it.govhub.security.beans.GovhubPrincipal;

@RestController
public class ProfileController implements ProfileApi {
	
	@Autowired
	UserAssembler userAssembler;

	@Override
	public ResponseEntity<Profile> profile() {
	
		// TODO: Prelevare solo le autorizzazioni che riguardano i ruoli di govregistry: govhub_organization_* govhub_service_* e govhub_user_*
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		
		Profile ret = this.userAssembler.toProfileModel(principal.getUser());
		
		return ResponseEntity.ok(ret);
	}

}
