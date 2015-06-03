package geoide.config;

import nl.idgis.geoide.commons.domain.api.MapQuery;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MapViewConfig {

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
}
