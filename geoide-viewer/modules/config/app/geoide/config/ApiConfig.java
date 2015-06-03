package geoide.config;

import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.MapProviderApi;
import nl.idgis.geoide.commons.domain.api.MapQuery;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.domain.api.ServiceProviderApi;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.commons.domain.api.TemplateDocumentProvider;
import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.Play;

@Configuration
public class ApiConfig {
	
	@Bean
	@Autowired
	public MapView mapView (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, MapView.class);
	}
	
	@Bean
	@Autowired
	public MapQuery mapQuery (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, MapQuery.class);
	}
	
	@Bean
	@Autowired
	public TableOfContents tableOfContents (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, TableOfContents.class);
	}
	
	@Bean
	@Autowired
	public ServiceProviderApi serviceProviderApi (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, ServiceProviderApi.class);
	}
	
	@Bean
	@Autowired
	public MapProviderApi mapProviderApi (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, MapProviderApi.class);
	}
	
	@Bean
	@Autowired
	@Qualifier ("printDocumentCache")
	public DocumentCache printDocumentCache (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, DocumentCache.class, Play.application ().configuration().getString ("geoide.web.print.documentCacheQualifier"));
	}
	
	@Bean
	@Autowired
	public PrintService htmlPrintService (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, PrintService.class);
	}
	
	@Bean
	@Autowired
	public ReportComposer reportComposer (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, ReportComposer.class);
	}
	
	@Bean
	@Autowired
	public TemplateDocumentProvider templateDocumentProvider (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, TemplateDocumentProvider.class);
	}
	
}
