package viewer.config;

import java.util.Collection;

import nl.idgis.geoide.commons.layer.DefaultLayerType;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LayerTypeConfig {

	@Configuration
	public static class LayerTypesConfig {
		@Bean
		@Autowired
		public DefaultLayerType defaultLayerType (final ServiceTypeRegistry serviceTypeRegistry) {
			return new DefaultLayerType (serviceTypeRegistry);
		}
	}
	
	@Bean
	@Autowired
	public LayerTypeRegistry layerTypeRegistry (final Collection<LayerType> layerTypes) {
		return new LayerTypeRegistry (layerTypes);
	}
}
