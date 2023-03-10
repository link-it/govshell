package it.govhub.govshell.proxy.beans;

import com.fasterxml.jackson.annotation.JsonRootName;

import it.govhub.govregistry.commons.entity.LogoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "application")
public class GovhubApplication {
	
	private String id;
	
	private String name;
	
	private String appUrl;
	
	private LogoEntity logo;
	
}