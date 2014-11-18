package nl.idgis.geoide.commons.layer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;

public final class DefaultLayerType extends LayerType {

	public DefaultLayerType (final ServiceTypeRegistry serviceTypeRegistry) {
		super (serviceTypeRegistry);
	}
	
	@Override
	public String getTypeName () {
		return "default";
	}

	@Override
	public Promise<List<ParameterizedServiceLayer<?>>> getServiceLayers (final Layer layer, final JsonNode state) {
		final List<ParameterizedServiceLayer<?>> result = new ArrayList<ParameterizedServiceLayer<?>> ();
		
		if (state.path ("visible").asBoolean ()) {
			for (final ServiceLayer serviceLayer: layer.getServiceLayers ()) {
				result.add (new ParameterizedServiceLayer<Object> (serviceLayer, null));
			}
		}
		
		return Promise.pure (Collections.unmodifiableList (result));
	}

	@Override
	public Promise<List<ParameterizedFeatureType<?>>> getFeatureTypes (final Layer layer, final FeatureQuery query, final JsonNode state) {
		final List<ParameterizedFeatureType<?>> result = new ArrayList<> ();
		
		for (final ServiceLayer serviceLayer: layer.getServiceLayers ()) {
			if (serviceLayer.getFeatureType () == null) {
				continue;
			}
			
			result.add (new ParameterizedFeatureType<Serializable> (serviceLayer, serviceLayer.getFeatureType (), query, null));
		}
		
		return Promise.pure (Collections.unmodifiableList (result));
	}
}
