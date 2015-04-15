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
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.service.ServiceTypeRegistry;

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
	public List<ParameterizedServiceLayer<?>> getServiceLayers (final Traits<LayerState> layerState) {
		final List<ParameterizedServiceLayer<?>> result = new ArrayList<ParameterizedServiceLayer<?>> ();
		
		if (layerState.get ().isVisible ()) {
			for (final ServiceLayer serviceLayer: layerState.get ().getLayer ().getServiceLayers ()) {
				result.add (new ParameterizedServiceLayer<Object> (serviceLayer, null));
			}
		}
		
		return Collections.unmodifiableList (result);
	}

	@Override
	public List<ParameterizedFeatureType<?>> getFeatureTypes (final Layer layer, final FeatureQuery query, final JsonNode state) {
		final List<ParameterizedFeatureType<?>> result = new ArrayList<> ();
		
		for (final ServiceLayer serviceLayer: layer.getServiceLayers ()) {
			if (serviceLayer.getFeatureType () == null) {
				continue;
			}
			
			result.add (new ParameterizedFeatureType<Serializable> (serviceLayer, serviceLayer.getFeatureType (), query, null));
		}
		
		return Collections.unmodifiableList (result);
	}

	@Override
	public Traits<LayerState> createLayerState (final Layer layer, final JsonNode state, final List<Traits<LayerState>> parentStates) {
		final List<Traits<LayerState>> parents = parentStates.isEmpty () || parentStates == null ? Collections.emptyList () : new ArrayList<> ();
		final boolean visible = state.path ("visible").asBoolean ();
		
		return Traits.create (new LayerState () {
			@Override
			public boolean isVisible () {
				return visible;
			}

			@Override
			public Layer getLayer () {
				return layer;
			}

			@Override
			public List<Traits<LayerState>> getParents() {
				return Collections.unmodifiableList (parents);
			}
		});
	}
}
