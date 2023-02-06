package it.govhub.govshell.proxy.security;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.messages.UserMessages;
import it.govhub.security.cache.Caches;
import it.govhub.security.repository.SecurityUserRepository;

/**
 * Mappa l'utente Ldap in un GovhubPrincipal, ignora gli attributi letti dallo
 * ldapUser. Se gli attributi non servono, allora implementare
 * UserDetailsContextMapper e restituire direttamente lo userDetails che ci
 * serve.
 * 
 * Se necessario restituire un LdapGovhubUser che ha anche il dn
 * 
 * String dn = ctx.getNameInNamespace();
 *
 */
@Service
public class LdapUserDetailsContextMapper extends LdapUserDetailsMapper {

	@Autowired
	SecurityUserRepository userRepo;

	@Autowired
	UserMessages userMessages;
	
	Logger logger = LoggerFactory.getLogger(LdapUserDetailsContextMapper.class);

	@Override
	@Cacheable(cacheNames = Caches.PRINCIPALS, key = "#username")
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
		logger.debug("Mappo Utenza Ldap in un GovhubPrincipal..");
		UserDetails details = super.mapUserFromContext(ctx, username, authorities);

		UserEntity user = this.userRepo.findAndPreloadByPrincipal(username)
				.orElseThrow(() -> new UsernameNotFoundException(this.userMessages.principalNotFound(username)));

		return new LdapGovhubPrincipal(user, ctx.getNameInNamespace());
	}

}
