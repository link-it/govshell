package it.govhub.govshell.proxy.security;

import org.springframework.security.ldap.userdetails.LdapUserDetails;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.security.beans.GovhubPrincipal;

public class LdapGovhubPrincipal extends GovhubPrincipal implements LdapUserDetails {

	private static final long serialVersionUID = 1L;
	
	private String dn;

	public LdapGovhubPrincipal(UserEntity user, String dn) {
		super(user);
		this.dn = dn;
	}

	@Override
	public void eraseCredentials() {
	}

	@Override
	public String getDn() {
		return this.dn;
	}

}
