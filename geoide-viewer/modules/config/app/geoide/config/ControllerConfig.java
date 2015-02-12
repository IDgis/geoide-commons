package geoide.config;

import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.commons.print.service.PrintService;
import nl.idgis.geoide.commons.report.ReportComposer;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.util.streams.StreamProcessor;

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
import controllers.printservice.Print;
import controllers.printservice.Report;



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
	
	@Bean
	@Autowired
	public Symbol symbolController (final MapProvider mapProvider, final ServiceTypeRegistry serviceTypeRegistry) {
		return new Symbol (mapProvider, serviceTypeRegistry);
	}
	
	@Bean
	@Autowired
	public Print printServiceController (
			final PrintService printService, 
			final @Qualifier ("printDocumentCache") DocumentCache documentCache,
			final StreamProcessor streamProcessor) {
		return new Print (printService, documentCache, streamProcessor);
	}
	
	@Bean
	@Autowired
	public Report reportController (
			final  @Qualifier ("reportComposer") ReportComposer composer
			) {
		return new Report (composer);
	}
	
}
