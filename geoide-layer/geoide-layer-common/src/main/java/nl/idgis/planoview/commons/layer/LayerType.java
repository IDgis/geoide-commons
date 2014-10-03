package nl.idgis.planoview.commons.layer;

import java.util.List;

import nl.idgis.planoview.commons.domain.FeatureQuery;
import nl.idgis.planoview.commons.domain.Layer;
import nl.idgis.planoview.commons.domain.ParameterizedFeatureType;
import nl.idgis.planoview.commons.domain.ParameterizedServiceLayer;
import nl.idgis.planoview.service.ServiceTypeRegistry;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class LayerType {
	
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
	
	public abstract List<ParameterizedServiceLayer<?>> getServiceLayers (final Layer layer, final JsonNode state);
	public abstract List<ParameterizedFeatureType<?>> getFeatureTypes (final Layer layer, final FeatureQuery query, final JsonNode state);
}
