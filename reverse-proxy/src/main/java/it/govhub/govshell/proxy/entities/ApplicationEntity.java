package it.govhub.govshell.proxy.entities;

import java.net.URI;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "govhub_applications")
public class ApplicationEntity {

	@Id
	private Long id;

	@EqualsAndHashCode.Include
	@Column(name = "application_id", unique=true, nullable = false)
	private String applicationId;
	
	@Column(name = "name",  nullable = false)
	private String name;
	
	@Column(name = "deployed_uri", nullable = false)
	//private URI deployedUri;
	private String deployedUri;
	
	@Embedded
	@AttributeOverrides({
		 @AttributeOverride(
		            name = "name",
		            column = @Column( name = "logo_name" )
		        ),
		 @AttributeOverride(
		            name = "url",
		            column = @Column( name = "logo_url" )
		        ),
		 @AttributeOverride(
		            name = "type",
		            column = @Column( name = "logo_type" )
		        ),
		 @AttributeOverride(
		            name = "color",
		            column = @Column( name = "logo_color" )
		        ),
		 @AttributeOverride(
		            name = "bg_color",
		            column = @Column( name = "logo_bg_color" )
		        )
	})
	private LogoEntity logo;
	
}