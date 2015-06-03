package geoide.config;

import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.MapProviderApi;
import nl.idgis.geoide.commons.domain.api.MapQuery;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.domain.api.ServiceProviderApi;
import nl.idgis.geoide.commons.domain.api.TemplateDocumentProvider;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorRef;
import controllers.core.MapConfiguration;
import controllers.mapview.Query;
import controllers.mapview.Services;
import controllers.mapview.View;
import controllers.printservice.Print;
import controllers.printservice.Report;
import controllers.printservice.Template;



@Configuration
public class ControllerConfig {

	@Bean
	@Autowired
	public MapConfiguration mapConfigurationController (final MapProviderApi mapProvider) {
		return new MapConfiguration (mapProvider);
	}
	
	@Bean
	@Autowired
	public View viewController (
		final MapView mapView) {
		return new View (mapView);
	}
	
	@Bean
	@Autowired
	public Services serviceController (final ServiceProviderApi serviceProvider, final @Qualifier("serviceManagerActor") ActorRef serviceManagerActor) {
		return new Services (serviceProvider, serviceManagerActor);
	}
	
	@Bean
	@Autowired
	public Query queryController (
			final MapQuery mapQuery,
			final @Qualifier("serviceManagerActor") ActorRef serviceManagerActor) {
		return new Query (mapQuery, serviceManagerActor);
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
			final  @Qualifier ("reportComposer") ReportComposer composer,
			final StreamProcessor streamProcessor,
			final @Qualifier ("printDocumentCache") DocumentCache documentCache
			) {
		return new Report (composer, streamProcessor, documentCache);
	}
	
	@Bean
	@Autowired
	public Template templateController (
			final TemplateDocumentProvider templateDocumentProvider
			) {
		return new Template (templateDocumentProvider);
	}
	
	
}
