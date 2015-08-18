package nl.idgis.geoide.commons.config;

import java.io.File;
import java.io.IOException;

import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.DocumentStore;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.print.service.HtmlPrintService;
import nl.idgis.geoide.commons.report.DefaultReportComposer;
import nl.idgis.geoide.commons.report.ReportPostProcessor;
import nl.idgis.geoide.commons.report.template.HtmlTemplateDocumentProvider;
import nl.idgis.geoide.documentcache.service.DefaultDocumentCache;
import nl.idgis.geoide.documentcache.service.DelegatingStore;
import nl.idgis.geoide.documentcache.service.FileStore;
import nl.idgis.geoide.documentcache.service.HttpDocumentStore;
import nl.idgis.geoide.map.DefaultMapView;
import nl.idgis.geoide.util.ConfigWrapper;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorSystem;

@Configuration
public class PrintConfig {
	@Bean
	@Qualifier ("printHttpDocumentStore")
	@Autowired
	public HttpDocumentStore printHttpDocumentStore (final HttpClient httpClient, final StreamProcessor streamProcessor) {
		return new HttpDocumentStore (httpClient, streamProcessor);
	}
	
	@Bean
	@Qualifier ("printFileStore")
	@Autowired
	public FileStore printFileStore (
			final @Qualifier("streamProcessor") StreamProcessor streamProcessor,
			final ConfigWrapper config) {
		String basePath = config.getString ("geoide.service.components.print.templatepath", "C:/Temp");
		String protocol = "template";
		return new FileStore(new File(basePath), protocol, streamProcessor);
	}
	
	@Bean
	@Qualifier ("delegatingDocumentStore")
	@Autowired
	public DelegatingStore delegatingStore (final @Qualifier("printHttpDocumentStore") HttpDocumentStore httpDocumentStore, final @Qualifier("printFileStore") FileStore fileStore) {
		DocumentStore[] stores = {httpDocumentStore, fileStore};
		return new DelegatingStore(stores);
	}
	
	@Bean
	@Qualifier ("printDocumentCache")
	@Autowired
	public DefaultDocumentCache printDocumentCache (
			final @Qualifier("delegatingDocumentStore") DelegatingStore documentStore, 
			final StreamProcessor streamProcessor,
			final ConfigWrapper config,
			final ActorSystem actorSystem) throws IOException {
		return DefaultDocumentCache.createTempFileCache (
				actorSystem, 
				streamProcessor, 
				config.getString ("geoide.service.components.print.cacheName", "geoide-print"), 
				config.getInt ("geoide.service.components.print.cacheTtlInSeconds", 300), 
				documentStore,
				config.getInt ("geoide.service.components.print.streamBlockSize", 100 * 1024)
			);
	}
	
	@Bean
	@Autowired
	public HtmlPrintService htmlPrintService (
			final @Qualifier ("printDocumentCache") DocumentCache documentCache, 
			final StreamProcessor streamProcessor,
			final ConfigWrapper config) {
		return new HtmlPrintService (
				documentCache, 
				streamProcessor, 
				config.getInt ("geoide.service.components.print.maxThreads", 2),
				config.getLong ("geoide.service.components.print.cacheTimeoutInMillis", 30000l)
			);
	}
	
	
	@Bean
	@Qualifier ("reportPostProcessor")
	@Autowired
	public ReportPostProcessor reportPostProcessor (HtmlPrintService htmlPrintService, final @Qualifier ("printDocumentCache") DefaultDocumentCache documentCache ) {
		return new ReportPostProcessor (
				htmlPrintService,
				documentCache
		);
	}
	
	
	
	@Bean
	@Qualifier ("templateDocumentProvider")
	@Autowired
	public HtmlTemplateDocumentProvider templateProvider(final @Qualifier("delegatingDocumentStore") DelegatingStore documentStore, final @Qualifier("printFileStore") FileStore fileStore, final @Qualifier("streamProcessor") StreamProcessor streamProcessor ) {
		return new HtmlTemplateDocumentProvider(documentStore, fileStore, streamProcessor);
	}
	
	
	@Bean
	@Qualifier ("reportComposer")
	@Autowired
	public ReportComposer reportComposer (final @Qualifier ("reportPostProcessor") ReportPostProcessor reportPostProcessor, HtmlTemplateDocumentProvider templateProvider, DefaultMapView mapView, final @Qualifier ("printDocumentCache") DocumentCache documentCache) {
		return new DefaultReportComposer (
				reportPostProcessor,
				templateProvider, 
				mapView,
				documentCache
			);
	}
}
