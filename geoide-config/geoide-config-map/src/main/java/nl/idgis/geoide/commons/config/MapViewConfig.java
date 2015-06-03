package nl.idgis.geoide.commons.config;

import nl.idgis.geoide.commons.domain.provider.LayerProvider;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.map.DefaultMapQuery;
import nl.idgis.geoide.map.DefaultMapView;
import nl.idgis.geoide.map.DefaultTableOfContents;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapViewConfig {

	@Bean
	@Autowired
	public DefaultMapView mapView (LayerTypeRegistry layerTypeRegistry, ServiceTypeRegistry serviceTypeRegistry, LayerProvider layerProvider) {
		return new DefaultMapView  (
				layerTypeRegistry,
				serviceTypeRegistry,
				layerProvider
			);
	}
	
	@Bean
	@Autowired
	public DefaultMapQuery mapQuery (final LayerTypeRegistry layerTypeRegistry, final LayerProvider layerProvider) {
		return new DefaultMapQuery (layerTypeRegistry, layerProvider);
	}
	
	@Bean
	@Autowired
	public DefaultTableOfContents tableOfContents (final LayerTypeRegistry layerTypeRegistry) {
		return new DefaultTableOfContents (layerTypeRegistry);
	}
}
