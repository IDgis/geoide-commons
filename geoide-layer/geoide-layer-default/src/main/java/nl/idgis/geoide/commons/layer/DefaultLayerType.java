package nl.idgis.geoide.commons.layer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.commons.domain.layer.LayerState;
import nl.idgis.geoide.commons.domain.traits.Traits;
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
		
		if (isEffectiveVisible (layerState)) {
			for (final ServiceLayer serviceLayer: layerState.get ().getLayer ().getServiceLayers ()) {
				result.add (new ParameterizedServiceLayer<Object> (serviceLayer, null));
			}
		}
		
		return Collections.unmodifiableList (result);
	}

	@Override
	public List<ParameterizedFeatureType<?>> getFeatureTypes (final Layer layer, final Optional<FeatureQuery> query, final JsonNode state) {
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
		final List<Traits<LayerState>> parents = parentStates.isEmpty () || parentStates == null ? Collections.emptyList () : new ArrayList<> (parentStates);
		final boolean visible = state.path ("visible").asBoolean ();
		
		return Traits.create (new LayerState (layer, visible, parents, Optional.of (state)));
	}
	
	private boolean isEffectiveVisible (final Traits<LayerState> layerState) {
		if (!layerState.get ().isVisible ()) {
			return false;
		}
		
		for (final Traits<LayerState> parentState: layerState.get ().getParents ()) {
			if (!parentState.get ().isVisible ()) {
				return false;
			}
		}
		
		return true;
	}
}
