package nl.idgis.geoide.commons.layer;

import java.util.List;

import play.libs.F.Promise;
import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
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
	
	public abstract Promise<List<ParameterizedServiceLayer<?>>> getServiceLayers (final Layer layer, final JsonNode state);
	public abstract Promise<List<ParameterizedFeatureType<?>>> getFeatureTypes (final Layer layer, final FeatureQuery query, final JsonNode state);
	
}
