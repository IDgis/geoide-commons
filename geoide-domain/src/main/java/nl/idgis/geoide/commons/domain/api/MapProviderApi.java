package nl.idgis.geoide.commons.domain.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import nl.idgis.geoide.commons.domain.LayerRef;
import nl.idgis.geoide.commons.domain.MapDefinition;

public interface MapProviderApi {

	CompletableFuture<MapDefinition> getMapDefinition(String mapId, String token);
	CompletableFuture<List<LayerRef>> getLayers(String mapId, String token);
	CompletableFuture<List<LayerRef>> getRootLayers(String mapId, String token);
	CompletableFuture<Boolean> refresh();
	CompletableFuture<String> getToken();
}
