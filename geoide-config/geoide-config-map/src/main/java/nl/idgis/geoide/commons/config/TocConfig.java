package nl.idgis.geoide.commons.config;


import nl.idgis.geoide.commons.domain.api.DocumentStore;
import nl.idgis.geoide.commons.domain.traits.spring.TypedTrait;
import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.layer.DefaultLayerType;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.toc.TOCdefaultLayerTypeTrait;
import nl.idgis.geoide.documentcache.service.DelegatingStore;
import nl.idgis.geoide.documentcache.service.FileStore;
import nl.idgis.geoide.documentcache.service.HttpDocumentStore;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.service.wms.WMSServiceType;
import nl.idgis.geoide.service.wms.toc.TOCwmsTrait;
import nl.idgis.geoide.toc.StoredImageProvider;
import nl.idgis.geoide.util.ConfigWrapper;
import nl.idgis.geoide.util.streams.StreamProcessor;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TocConfig {
	
	@Bean
	@Autowired
	public HttpDocumentStore legendHttpDocumentStore (final HttpClient httpClient, final StreamProcessor streamProcessor) {
		return new HttpDocumentStore (httpClient, streamProcessor);
	}
	
	
	@Bean
	@Autowired
	public FileStore imageFileStore (
			final @Qualifier("streamProcessor") StreamProcessor streamProcessor,
			final ConfigWrapper config) {
		String basePath = config.getString ("geoide.service.components.imageProvider.dir", "C:/Temp");
		String protocol = "image";
		return new FileStore(new File(basePath), protocol, streamProcessor);
	}
	
	
	@Bean
	@Autowired
	public DelegatingStore legendDocumentStore (final @Qualifier("legendHttpDocumentStore") HttpDocumentStore httpDocumentStore, final @Qualifier("imageFileStore") FileStore fileStore) {
		DocumentStore[] stores = {httpDocumentStore, fileStore};
		return new DelegatingStore(stores);
	}
	

	@Bean
	@Autowired
	public StoredImageProvider imageProvider(final @Qualifier("legendDocumentStore") DocumentStore documentStore, final @Qualifier("streamProcessor") StreamProcessor streamProcessor ) {
		return new StoredImageProvider(documentStore,  streamProcessor);
	}
	
	
	
	@Autowired
	@Bean
	@Qualifier ("layerTypeTrait")
	public TypedTrait<LayerType, DefaultLayerType> tocDefaultLayerTypeTrait (ServiceTypeRegistry serviceTypeRegistry) {
		return TypedTrait.create(DefaultLayerType.class, new TOCdefaultLayerTypeTrait(serviceTypeRegistry));	
	}
	
	@Autowired
	@Bean
	@Qualifier ("serviceTypeTrait")
	public TypedTrait<ServiceType, WMSServiceType> tocWmsTrait () {
		return TypedTrait.create(WMSServiceType.class, new TOCwmsTrait());
	}
}
