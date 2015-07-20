package nl.idgis.geoide.commons.domain.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.MapDefinition;

public interface MapProviderApi {

	CompletableFuture<MapDefinition> getMapDefinition(String mapId);
	CompletableFuture<List<Layer>> getLayers(String mapId);
	CompletableFuture<List<Layer>> getRootLayers(String mapId);
}