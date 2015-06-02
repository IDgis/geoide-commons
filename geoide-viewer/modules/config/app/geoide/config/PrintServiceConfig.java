package geoide.config;

import nl.idgis.geoide.commons.print.service.PrintService;
import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;
import nl.idgis.geoide.documentcache.DocumentCache;

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
	
	/*
	@Bean
	@Qualifier ("printHttpDocumentStore")
	@Autowired
	public HttpDocumentStore printHttpDocumentStore (final HttpClient httpClient) {
		return new HttpDocumentStore (httpClient);
	}
	
	@Bean
	@Qualifier ("printFileStore")
	@Autowired
	public FileStore printFileStore (final @Qualifier("streamProcessor") StreamProcessor streamProcessor) {
		String basePath = Play.application ().configuration ().getString ("geoide.services.print.templatepath", "C:/Temp");
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
	public DefaultDocumentCache printDocumentCache (final @Qualifier("delegatingDocumentStore") DelegatingStore documentStore, final StreamProcessor streamProcessor) throws IOException {
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
	public ReportComposer reportComposer (final @Qualifier ("reportPostProcessor") ReportPostProcessor reportPostProcessor, HtmlTemplateDocumentProvider templateProvider, MapView mapView, final @Qualifier ("printDocumentCache") DocumentCache documentCache) {
		return new ReportComposer (
				reportPostProcessor,
				templateProvider, 
				null,//mapView,
				documentCache
			);
	}

	*/
}