package nl.idgis.geoide.commons.domain.provider;

import java.util.List;
import java.util.function.Supplier;

import nl.idgis.geoide.commons.domain.FeatureType;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.LayerRef;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.SearchTemplate;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceLayer;

public class ReloadableStaticMapProvider implements MapProvider, ServiceProvider, ServiceLayerProvider, FeatureTypeProvider, LayerProvider, SearchTemplateProvider {

	private final Supplier<StaticMapProvider> supplier;
	
	private StaticMapProvider mapProvider;

	public ReloadableStaticMapProvider(Supplier<StaticMapProvider> supplier) {
		this.supplier = supplier;
		load();
	}
	
	private synchronized void load() {
		mapProvider = supplier.get();
	}
	
	private synchronized StaticMapProvider getMapProvider() {
		return mapProvider;
	}
	
	@Override
	public void reload() {
		load();
	}

	@Override
	public MapDefinition getMapDefinition(String mapId) {
		return getMapProvider().getMapDefinition(mapId);
	}

	@Override
	public List<LayerRef> getLayers(String mapId) {
		return getMapProvider().getLayers(mapId);
	}

	@Override
	public List<LayerRef> getRootLayers(String mapId) {
		return getMapProvider().getRootLayers(mapId);
	}

	@Override
	public List<SearchTemplate> getSearchTemplates(String mapId) {
		return getMapProvider().getSearchTemplates(mapId);
	}

	@Override
	public SearchTemplate getSearchTemplate(String searchTemplateId) {
		return getMapProvider().getSearchTemplate(searchTemplateId);
	}

	@Override
	public Layer getLayer(String layer) {
		return getMapProvider().getLayer(layer);
	}

	@Override
	public List<Layer> getLayers(List<String> layerIds) {
		return getMapProvider().getLayers(layerIds);
	}

	@Override
	public ServiceLayer getServiceLayer(String serviceLayerId) {
		return getMapProvider().getServiceLayer(serviceLayerId);
	}

	@Override
	public List<ServiceLayer> getServiceLayers(List<String> serviceLayerId) {
		return getMapProvider().getServiceLayers(serviceLayerId);
	}

	@Override
	public FeatureType getFeatureType(String serviceLayerId) {
		return getMapProvider().getFeatureType(serviceLayerId);
	}

	@Override
	public Service getService(String serviceId) {
		return getMapProvider().getService(serviceId);
	}
}
