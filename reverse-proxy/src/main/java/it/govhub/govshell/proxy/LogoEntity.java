package it.govhub.govshell.proxy;

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;

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
@Embeddable
public class LogoEntity {
	
	@Enumerated
	@Column(name="type", nullable=false)
	private LogoType type;

	@Column(name="url")
	private URL url;
	
	// Utilizzati per type == Svg/Bootstrap/Material
	
	@Column(name="color")
	private String color;
	
	@Column(name="bg_color")
	private String bgColor;
	
	// Utilizzato per type == Bootstrap/Maetrial
	
	@Column(name="name")
	private String name;
	
	public static enum LogoType {
		IMAGE, SVG, BOOTSTRAP, MATERIAL
	}

}


/*
 * /*
 * {

  "logo": {
    "url": "http://...",
    "icon": "bootstrap_icon_name",
    "micon": "material_icon_name",
    "icon_url": "http://.../file_name.svg", 
    "bg_color": "#0f72a7",
    "color": "#ffffff"
  }
}*/
