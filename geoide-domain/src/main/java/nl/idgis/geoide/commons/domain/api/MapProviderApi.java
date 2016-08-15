package nl.idgis.geoide.commons.domain.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.LayerRef;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.SearchTemplate;

public interface MapProviderApi {

	CompletableFuture<MapDefinition> getMapDefinition(String mapId);
	CompletableFuture<List<LayerRef>> getLayers(String mapId);
	CompletableFuture<List<LayerRef>> getRootLayers(String mapId);
	CompletableFuture<List<SearchTemplate>> getSearchTemplates(String mapId);
	CompletableFuture<Void> refresh();
}
