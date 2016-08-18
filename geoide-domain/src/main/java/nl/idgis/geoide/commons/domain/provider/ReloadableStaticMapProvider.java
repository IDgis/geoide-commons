package nl.idgis.geoide.commons.domain.provider;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import nl.idgis.geoide.commons.domain.FeatureType;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.LayerRef;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceLayer;

public class ReloadableStaticMapProvider implements MapProvider, ServiceProvider, ServiceLayerProvider, FeatureTypeProvider, LayerProvider {

	private final Supplier<StaticMapProvider> supplier;
	
	private String currentToken = "";
	private StaticMapProvider mapProvider;

	public ReloadableStaticMapProvider(Supplier<StaticMapProvider> supplier) {
		this.supplier = supplier;
		load();
	}
	
	private synchronized void load() {
		UUID uuid = UUID.randomUUID();
		currentToken = uuid.toString();
		mapProvider = supplier.get();
	}
	
	private synchronized StaticMapProvider getMapProvider (String token) {
		if(!token.equals(currentToken)) {
			throw new IllegalStateException("wrong token: " + token);
		}; 
		 
		return mapProvider;
	}
	
	@Override
	public boolean reload() {
		load();
		return true;
	}
	
	@Override 	
	public synchronized String getToken() {
		return currentToken;
	}

	@Override
	public MapDefinition getMapDefinition(String mapId, String token) {
		return getMapProvider(token).getMapDefinition(mapId, token);
	}

	@Override
	public List<LayerRef> getLayers(String mapId, String token) {
		return getMapProvider(token).getLayers(mapId, token);
	}

	@Override
	public List<LayerRef> getRootLayers(String mapId, String token) {
		return getMapProvider(token).getRootLayers(mapId, token);
	}


	@Override
	public Layer getLayer(String layer, String token) {
		return getMapProvider(token).getLayer(layer, token);
	}

	@Override
	public List<Layer> getLayers(List<String> layerIds, String token) {
		return getMapProvider(token).getLayers(layerIds, token);
	}

	@Override
	public ServiceLayer getServiceLayer(String serviceLayerId, String token) {
		return getMapProvider(token).getServiceLayer(serviceLayerId, token);
	}

	@Override
	public List<ServiceLayer> getServiceLayers(List<String> serviceLayerId, String token) {
		return getMapProvider(token).getServiceLayers(serviceLayerId, token);
	}

	@Override
	public FeatureType getFeatureType(String serviceLayerId, String token) {
		return getMapProvider(token).getFeatureType(serviceLayerId, token);
	}

	@Override
	public Service getService(String serviceId, String token) {
		return getMapProvider(token).getService(serviceId, token);
	}

}
