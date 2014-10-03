package nl.idgis.geoide.commons.domain.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.util.Assert;

public class StaticMapProvider implements MapProvider {

	private final MapDefinition mapDefinition;
	
	public StaticMapProvider (final MapDefinition mapDefinition) {
		Assert.notNull (mapDefinition, "mapDefinition");
		
		this.mapDefinition = mapDefinition;
	}
	
	public StaticMapProvider (final String jsonData) {
		this (JsonFactory.mapDefinition (jsonData));
	}
	
	public StaticMapProvider (final InputStream inputStream) {
		this (JsonFactory.mapDefinition (inputStream));
	}
	
	public StaticMapProvider (final JsonNode json) {
		this (JsonFactory.mapDefinition (json));
	}
	
	@Override
	public MapDefinition getMapDefinition (final String mapId) {
		if (mapDefinition.getId ().equals (mapId)) {
			return mapDefinition;
		}
		
		return null;
	}

	@Override
	public Layer getLayer (final String layerId) {
		if (layerId == null) {
			return null;
		}
		
		return mapDefinition.getLayers ().get (layerId);
	}

	@Override
	public List<Layer> getLayers (final List<String> layerIds) {
		if (layerIds == null || layerIds.isEmpty ()) {
			return Collections.emptyList ();
		}
		
		final List<Layer> layers = new ArrayList<Layer> (layerIds.size ());
		
		for (final String layerId: layerIds) {
			layers.add (getLayer (layerId));
		}
		
		return Collections.unmodifiableList (layers);
	}

	@Override
	public Service getService (final String serviceId) {
		if (serviceId == null) {
			return null;
		}
		
		return mapDefinition.getServices ().get (serviceId);
	}
}
