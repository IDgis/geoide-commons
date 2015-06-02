package geoide.config;

import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.Play;

@Configuration
public class PrintServiceConfig {

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
}