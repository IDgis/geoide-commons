package nl.idgis.geoide.commons.config;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.idgis.geoide.commons.domain.LayerRef;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.SearchTemplate;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.api.MapProviderApi;
import nl.idgis.geoide.commons.domain.api.ServiceProviderApi;
import nl.idgis.geoide.commons.domain.provider.LayerProvider;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.domain.provider.ServiceProvider;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.map.DefaultMapQuery;
import nl.idgis.geoide.map.DefaultMapView;
import nl.idgis.geoide.map.DefaultTableOfContents;
import nl.idgis.geoide.service.ServiceTypeRegistry;

@Configuration
public class MapConfig {

	@Bean
	@Autowired
	public DefaultMapView mapView (LayerTypeRegistry layerTypeRegistry, ServiceTypeRegistry serviceTypeRegistry, LayerProvider layerProvider) {
		return new DefaultMapView  (
				layerTypeRegistry,
				serviceTypeRegistry,
				layerProvider
			);
	}
	
	@Bean
	@Autowired
	public DefaultMapQuery mapQuery (final LayerTypeRegistry layerTypeRegistry, final LayerProvider layerProvider) {
		return new DefaultMapQuery (layerTypeRegistry, layerProvider);
	}
	
	@Bean
	@Autowired
	public DefaultTableOfContents tableOfContents (final LayerTypeRegistry layerTypeRegistry) {
		return new DefaultTableOfContents (layerTypeRegistry);
	}
	
	@Bean
	@Autowired
	public ServiceProviderApi serviceProviderApi (final ServiceProvider serviceProvider) {
		return (id, token) -> {
			final Service service = serviceProvider.getService (id, token);
			if (service == null) {
				return CompletableFuture.completedFuture (null);
			}
			
			return CompletableFuture.completedFuture (service.getIdentification ());
		};
	}
	
	@Bean
	@Autowired
	public MapProviderApi mapProviderApi (final MapProvider mapProvider) {
		return new MapProviderApi () {
			@Override
			public CompletableFuture<List<LayerRef>> getRootLayers (final String mapId, final String token) {
				return CompletableFuture.completedFuture (mapProvider.getRootLayers (mapId, token));
			}
			
			@Override
			public CompletableFuture<MapDefinition> getMapDefinition (final String mapId, final String token) {
				return CompletableFuture.completedFuture (mapProvider.getMapDefinition (mapId, token));
			}
			
			@Override
			public CompletableFuture<List<LayerRef>> getLayers (final String mapId, final String token) {
				return CompletableFuture.completedFuture (mapProvider.getLayers (mapId, token));
			}

			@Override
			public CompletableFuture<Boolean> refresh() {
				return CompletableFuture.supplyAsync(() -> mapProvider.reload());
			}
			
			@Override
			public CompletableFuture<String> getToken() {
				return CompletableFuture.supplyAsync(() -> mapProvider.getToken());
			}
		};
	}
}
