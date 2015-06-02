package nl.idgis.geoide.commons.config;

import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.commons.domain.provider.LayerProvider;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.map.DefaultMapView;
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
	
}
