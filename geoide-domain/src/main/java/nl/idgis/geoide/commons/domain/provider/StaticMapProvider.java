package nl.idgis.geoide.commons.domain.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.idgis.geoide.commons.domain.FeatureType;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.util.Assert;



import com.fasterxml.jackson.databind.JsonNode;

public class StaticMapProvider implements MapProvider, ServiceProvider, ServiceLayerProvider, FeatureTypeProvider, LayerProvider {

	private final Set<MapDefinition> mapDefinitions;
	
	public StaticMapProvider (final Collection<MapDefinition> mapDefinitions) {
		Assert.notNull (mapDefinitions, "mapDefinitions");
		
		this.mapDefinitions = new HashSet<> (mapDefinitions);
	}
	
	public StaticMapProvider (final MapDefinition mapDefinition) {
		this (wrap (mapDefinition));
	}
	
	public StaticMapProvider (final String jsonData) {
		this (JsonFactory.mapDefinition (jsonData));
	}
	
	public StaticMapProvider (final JsonNode json) {
		this (JsonFactory.mapDefinition (json));
	}
	
	
	public StaticMapProvider (final InputStream ... inputStreams) {
		this (makeMapDefinitions (inputStreams));
	}

	
	private static Collection<MapDefinition> makeMapDefinitions (final InputStream[] inputStreams) {
		Assert.notNull (inputStreams, "inputStream");
		
		final List<MapDefinition> mapDefinitions = JsonFactory.mapDefinitions (inputStreams);
		
		return mapDefinitions;
	}
	
	
	private static <T> Collection<T> wrap (final T value) {
		Assert.notNull (value, "value");
		
		final List<T> list = new ArrayList<> (1);
		
		list.add (value);
		
		return Collections.unmodifiableList (list);
	}
	
	@Override
	public MapDefinition getMapDefinition (final String mapId) {
		for (final MapDefinition mapDefinition: mapDefinitions) {
			if (mapDefinition.getId ().equals (mapId)) {
				return mapDefinition;
			}
		}
		
		return null;
	}
	
	@Override
	public List<Layer> getLayers(String mapId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Layer> getRootLayers(String mapId) {
		for (final MapDefinition mapDefinition: mapDefinitions) {
			if (mapDefinition.getId ().equals (mapId)) {
				return mapDefinition.getRootLayers();
			}
		}
		return null;
	}
	

	@Override
	public Layer getLayer (final String layerId) {
		if (layerId == null) {
			return null;
		}
		
		for (final MapDefinition mapDefinition: mapDefinitions) {
			final Layer layer = mapDefinition.getLayers ().get (layerId);
			
			if (layer != null) {
				return layer;
			}
		}
		
		return null;
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
		
		for (final MapDefinition mapDefinition: mapDefinitions) {
			final Service service = mapDefinition.getServices ().get (serviceId);
			
			if (service != null) {
				return service;
			}
		}
		
		return null;
	}
	
	@Override
	public ServiceLayer getServiceLayer (final String serviceLayerId) {
		if (serviceLayerId == null) {
			return null;
		}
		
		for (final MapDefinition mapDefinition: mapDefinitions) {
			final ServiceLayer serviceLayer = mapDefinition.getServiceLayers ().get (serviceLayerId);
			
			if (serviceLayer != null) {
				return serviceLayer;
			}
		}
		
		return null; 
	}


	@Override
	public List<ServiceLayer> getServiceLayers(List<String> serviceLayerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FeatureType getFeatureType(String featureTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
