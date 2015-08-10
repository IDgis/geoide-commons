package nl.idgis.geoide.commons.domain.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.layer.LayerState;
import nl.idgis.geoide.commons.domain.traits.Traits;

public interface MapView {

	CompletableFuture<List<ServiceRequest>> getServiceRequests (List<Traits<LayerState>> layerStates);

	CompletableFuture<List<Traits<LayerState>>> flattenLayerList (ExternalizableJsonNode viewerState);

}