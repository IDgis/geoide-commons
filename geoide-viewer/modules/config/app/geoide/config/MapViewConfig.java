package geoide.config;

import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.map.MapView;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MapViewConfig {

	@Bean
	@Autowired
	public MapView mapView (LayerTypeRegistry layerTypeRegistry, ServiceTypeRegistry serviceTypeRegistry, MapProvider mapProvider) {
		return new MapView  (
				layerTypeRegistry,
				serviceTypeRegistry,
				mapProvider
			);
	}
	
}
