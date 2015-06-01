package nl.idgis.geoide.commons.config;

import nl.idgis.geoide.commons.domain.provider.StaticMapProvider;
import nl.idgis.geoide.util.ConfigWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {

	private final static Logger log = LoggerFactory.getLogger (ProviderConfig.class);
	
	@Bean
	@Autowired
	public StaticMapProvider mapProvider (final ConfigWrapper config) {
		final String mapsResource = config.getString ("geoide.service.components.mapProvider.resources.maps", "nl/idgis/geoide/commons/config/map/maps.json");
		final String servicesResource = config.getString ("geoide.service.components.mapProvider.resources.services", "nl/idgis/geoide/commons/config/map/services.json");
		final String featureTypesResource = config.getString ("geoide.service.components.mapProvider.resources.featureTypes", "nl/idgis/geoide/commons/config/map/featuretypes.json");
		final String serviceLayersResource = config.getString ("geoide.service.components.mapProvider.resources.serviceLayers", "nl/idgis/geoide/commons/config/map/servicelayers.json");
		final String layersResource = config.getString ("geoide.service.components.mapProvider.resources.layers", "nl/idgis/geoide/commons/config/map/layers.json");
		
		final ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
		
		log.info ("Loading static map configuration from classpath");
		log.info ("Maps configuration: " + mapsResource);
		log.info ("Services configuration: " + servicesResource);
		log.info ("Feature types configuration: " + featureTypesResource);
		log.info ("Service layers configuration: " + serviceLayersResource);
		log.info ("Layers configuration: " + layersResource);
		
		return new StaticMapProvider (cl.getResourceAsStream (mapsResource),
				cl.getResourceAsStream (servicesResource),
				cl.getResourceAsStream (featureTypesResource),
				cl.getResourceAsStream (serviceLayersResource),
				cl.getResourceAsStream (layersResource));
	}
}
