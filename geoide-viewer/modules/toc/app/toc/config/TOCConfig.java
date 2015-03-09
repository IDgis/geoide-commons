package toc.config;

import nl.idgis.geoide.commons.layer.LayerTypeRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import controllers.toc.TOC;

@Configuration
public class TOCConfig {
	
	@Bean
	@Autowired
	public TOC toc (LayerTypeRegistry layerTypeRegistry) {
		return new TOC(layerTypeRegistry);
	}
	
	

}
