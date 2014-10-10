package geoide.config;

import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorRef;
import controllers.mapview.MapConfiguration;
import controllers.mapview.Query;
import controllers.mapview.Services;
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
			final MapProvider mapProvider) {
		return new View (layerTypeRegistry, serviceTypeRegistry, mapProvider);
	}
	
	@Bean
	@Autowired
	public Services serviceController (final MapProvider mapProvider, final @Qualifier("serviceManagerActor") ActorRef serviceManagerActor) {
		return new Services (mapProvider, serviceManagerActor);
	}
	
	@Bean
	@Autowired
	public Query queryController (
			final LayerTypeRegistry layerTypeRegistry, 
			final MapProvider mapProvider,
			final @Qualifier("serviceManagerActor") ActorRef serviceManagerActor) {
		return new Query (layerTypeRegistry, mapProvider, serviceManagerActor);
	}
}