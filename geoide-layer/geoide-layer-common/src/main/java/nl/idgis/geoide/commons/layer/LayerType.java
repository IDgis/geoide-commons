package nl.idgis.geoide.commons.layer;

import java.util.List;
import java.util.Optional;

import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.layer.LayerState;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class LayerType  {
	
	private final ServiceTypeRegistry serviceTypeRegistry;
	
	public LayerType (final ServiceTypeRegistry serviceTypeRegistry) {
		if (serviceTypeRegistry == null) {
			throw new NullPointerException ("serviceTypeRegistry cannot be null");
		}
		
		this.serviceTypeRegistry = serviceTypeRegistry;
	}
	
	public ServiceTypeRegistry getServiceTypeRegistry () {
		return serviceTypeRegistry;
	}
	
	public abstract String getTypeName ();
	
	public abstract List<ParameterizedServiceLayer<?>> getServiceLayers (final Traits<LayerState> state);
	public abstract List<ParameterizedFeatureType<?>> getFeatureTypes (final Layer layer, final Optional<FeatureQuery> query, final JsonNode state);
	public abstract Traits<LayerState> createLayerState (Layer layer, JsonNode state, List<Traits<LayerState>> parents);
}
