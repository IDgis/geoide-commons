package nl.idgis.geoide.commons.domain.api;

import java.util.List;

import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.layer.LayerState;
import nl.idgis.geoide.commons.domain.traits.Traits;

import com.fasterxml.jackson.databind.JsonNode;

public interface MapView {

	List<ServiceRequest> getServiceRequests(List<Traits<LayerState>> layerStates);

	List<Traits<LayerState>> flattenLayerList(JsonNode viewerState);

}