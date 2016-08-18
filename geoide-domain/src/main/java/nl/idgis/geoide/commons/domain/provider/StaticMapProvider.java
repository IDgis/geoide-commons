package nl.idgis.geoide.commons.domain.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import nl.idgis.geoide.commons.domain.FeatureType;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.LayerRef;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.SearchTemplate;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.util.Assert;

public class StaticMapProvider implements MapProvider, ServiceProvider, ServiceLayerProvider, FeatureTypeProvider, LayerProvider {

	private final Set<MapDefinition> mapDefinitions;
	
	public StaticMapProvider (final Collection<MapDefinition> mapDefinitions) {
		Assert.notNull (mapDefinitions, "mapDefinitions");
		
		this.mapDefinitions = new HashSet<> (mapDefinitions);
	}
	
	public StaticMapProvider (final MapDefinition mapDefinition) {
		this (wrap (mapDefinition));
	}
	
	private static <T> Collection<T> wrap (final T value) {
		Assert.notNull (value, "value");
		
		final List<T> list = new ArrayList<> (1);
		
		list.add (value);
		
		return Collections.unmodifiableList (list);
	}
	
	@Override
	public MapDefinition getMapDefinition (final String mapId, final String token) {
		for (final MapDefinition mapDefinition: mapDefinitions) {
			if (mapDefinition.getId ().equals (mapId)) {
				return mapDefinition;
			}
		}
		
		return null;
	}
	
	@Override
	public List<LayerRef> getLayers(String mapId, final String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LayerRef> getRootLayers(String mapId, final String token) {
		for (final MapDefinition mapDefinition: mapDefinitions) {
			if (mapDefinition.getId ().equals (mapId)) {
				return mapDefinition.getRootLayers();
			}
		}
		return null;
	}
	

	@Override
	public Layer getLayer (final String layerId, final String token) {
		if (layerId == null) {
			return null;
		}
		for (final MapDefinition mapDefinition: mapDefinitions) {
			Iterator<Entry<String, LayerRef>> layerRefs = mapDefinition.getLayerRefs ().entrySet().iterator();
			while (layerRefs.hasNext()) {
				  Entry<String, LayerRef> layerRef = layerRefs.next();
				  if (layerId.equals(layerRef.getValue().getLayer().getId())) {
					  return layerRef.getValue().getLayer();
				  }
			}
		}
		
		return null;
	}

	@Override
	public List<Layer> getLayers (final List<String> layerIds, final String token) {
		if (layerIds == null || layerIds.isEmpty ()) {
			return Collections.emptyList ();
		}
		
		final List<Layer> layers = new ArrayList<Layer> (layerIds.size ());
		
		for (final String layerId: layerIds) {
			layers.add (getLayer (layerId, token));
		}
		
		return Collections.unmodifiableList (layers);
	}

	@Override
	public Service getService (final String serviceId, final String token) {
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
	public ServiceLayer getServiceLayer (final String serviceLayerId, final String token) {
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
	public List<ServiceLayer> getServiceLayers(List<String> serviceLayerId, final String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FeatureType getFeatureType(String featureTypeId, final String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean reload() {
		// reload not implemented for StaticMapProvider	
		return false;
	}
	

	@Override
	public String getToken() {
		// reload not implemented for StaticMapProvider	
		return null;
	}

	
}
