package toc.config;

import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import controllers.toc.TOC;

@Configuration
public class TOCConfig {
	
	@Bean
	@Autowired
	public TOC toc (MapProvider mapProvider, LayerTypeRegistry layerTypeRegistry, ServiceTypeRegistry serviceTypeRegistry) {
		return new TOC(mapProvider, layerTypeRegistry, serviceTypeRegistry);
	}
	
	

}
