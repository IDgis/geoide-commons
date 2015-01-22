package geoide.config;

import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.domain.provider.LayerProvider;
import nl.idgis.geoide.commons.domain.provider.ServiceLayerProvider;
import nl.idgis.geoide.commons.domain.provider.ServiceProvider;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorRef;
import controllers.core.MapConfiguration;
import controllers.mapview.Query;
import controllers.mapview.Services;
import controllers.mapview.Symbol;
import controllers.mapview.View;

@Configuration
public class ControllerConfig {

	@Bean
	@Autowired
	public MapConfiguration mapConfigurationController (final MapProvider mapProvider) {
		return new MapConfiguration (mapProvider);
	}
	
	@Bean
	@Autowired
	public View viewController (
			final LayerTypeRegistry layerTypeRegistry, 
			final ServiceTypeRegistry serviceTypeRegistry, 
			final LayerProvider layerProvider) {
		return new View (layerTypeRegistry, serviceTypeRegistry, layerProvider);
	}
	
	@Bean
	@Autowired
	public Services serviceController (final ServiceProvider serviceProvider, final @Qualifier("serviceManagerActor") ActorRef serviceManagerActor) {
		return new Services (serviceProvider, serviceManagerActor);
	}
	
	@Bean
	@Autowired
	public Query queryController (
			final LayerTypeRegistry layerTypeRegistry, 
			final LayerProvider layerProvider,
			final @Qualifier("serviceManagerActor") ActorRef serviceManagerActor) {
		return new Query (layerTypeRegistry, layerProvider, serviceManagerActor);
	}
	
	@Bean
	@Autowired
	public Symbol symbolController (final ServiceLayerProvider serviceLayerProvider, final ServiceTypeRegistry serviceTypeRegistry) {
		return new Symbol (serviceLayerProvider, serviceTypeRegistry);
	}
}
