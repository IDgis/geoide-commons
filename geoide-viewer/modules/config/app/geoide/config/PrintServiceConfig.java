package geoide.config;

import java.io.IOException;

import nl.idgis.geoide.commons.print.service.HtmlPrintService;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.geoide.documentcache.service.DefaultDocumentCache;
import nl.idgis.geoide.documentcache.service.HttpDocumentStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.libs.Akka;

@Configuration
public class PrintServiceConfig {

	@Bean
	@Qualifier ("printHttpDocumentStore")
	public DocumentStore printHttpDocumentStore () {
		return new HttpDocumentStore (20000);
	}
	
	@Bean
	@Qualifier ("printDocumentCache")
	@Autowired
	public DefaultDocumentCache printDocumentCache (final @Qualifier("printHttpDocumentStore") DocumentStore documentStore) throws IOException {
		return DefaultDocumentCache.createTempFileCache (Akka.system (), "geoide-print", 120, documentStore);
	}
	
	@Bean
	@Autowired
	public HtmlPrintService htmlPrintService (final @Qualifier ("printDocumentCache") DocumentCache documentCache) {
		return new HtmlPrintService (documentCache, 2, 30000);
	}
}