package geoide.config;

import java.io.IOException;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.map.MapView;
import nl.idgis.geoide.commons.print.service.HtmlPrintService;
import nl.idgis.geoide.commons.report.ReportComposer;
import nl.idgis.geoide.commons.report.ReportPostProcessor;
import nl.idgis.geoide.commons.report.template.HtmlTemplateDocumentProvider;
import nl.idgis.geoide.commons.report.template.TemplateDocumentProvider;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.geoide.documentcache.service.DefaultDocumentCache;
import nl.idgis.geoide.documentcache.service.HttpDocumentStore;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.Play;
import play.libs.Akka;

@Configuration
public class PrintServiceConfig {

	@Bean
	@Qualifier ("printHttpDocumentStore")
	@Autowired
	public DocumentStore printHttpDocumentStore (final HttpClient httpClient) {
		return new HttpDocumentStore (httpClient);
	}
	
	@Bean
	@Qualifier ("printDocumentCache")
	@Autowired
	public DefaultDocumentCache printDocumentCache (final @Qualifier("printHttpDocumentStore") DocumentStore documentStore, final StreamProcessor streamProcessor) throws IOException {
		return DefaultDocumentCache.createTempFileCache (
				Akka.system (), 
				streamProcessor, 
				Play.application ().configuration ().getString ("geoide.services.print.cacheName", "geoide-print"), 
				Play.application ().configuration ().getInt ("geoide.services.print.cacheTtlInSeconds", 300).intValue (), 
				documentStore
			);
	}
	
	@Bean
	@Autowired
	public HtmlPrintService htmlPrintService (final @Qualifier ("printDocumentCache") DocumentCache documentCache, final StreamProcessor streamProcessor) {
		return new HtmlPrintService (
				documentCache, 
				streamProcessor, 
				Play.application ().configuration ().getInt ("geoide.services.print.maxThreads", 2).intValue (),
				Play.application ().configuration ().getLong ("geoide.services.print.cacheTimeoutInMillis", 30000l).longValue ()
			);
	}
	
	
	@Bean
	@Qualifier ("reportPostProcessor")
	@Autowired
	public ReportPostProcessor reportPostProcessor (HtmlPrintService htmlPrintService, final @Qualifier ("printDocumentCache") DocumentCache documentCache ) {
		return new ReportPostProcessor (
				htmlPrintService,
				documentCache
		);
	}
	
	@Bean
	@Autowired
	public TemplateDocumentProvider templateProvider() {
		return new HtmlTemplateDocumentProvider();
	}
	
	
	@Bean
	@Qualifier ("reportComposer")
	@Autowired
	public ReportComposer reportComposer (final @Qualifier ("reportPostProcessor") ReportPostProcessor reportPostProcessor, TemplateDocumentProvider templateProvider, MapView mapView, final @Qualifier ("printDocumentCache") DocumentCache documentCache) {
		return new ReportComposer (
				reportPostProcessor,
				templateProvider, 
				mapView,
				documentCache
			);
	}
	

}