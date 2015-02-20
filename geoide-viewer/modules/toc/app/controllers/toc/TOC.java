package controllers.toc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.commons.layer.toc.TOCLayerTypeTrait;
import nl.idgis.geoide.service.ServiceTypeRegistry;


public class TOC  {

	private final MapProvider mapProvider;
	private final LayerTypeRegistry layerTypeRegistry;
	private final ServiceTypeRegistry serviceTypeRegistry;
	
	public TOC (final MapProvider mapProvider, final LayerTypeRegistry layerTypeRegistry, final ServiceTypeRegistry serviceTypeRegistry) {
		this.mapProvider = mapProvider;
		this.layerTypeRegistry = layerTypeRegistry;
		this.serviceTypeRegistry = serviceTypeRegistry;
	}
	
	
	public List<Traits<TOCItem>> getItems (final MapDefinition mapDefinition) {
		List<Traits<TOCItem>> tocItems = new ArrayList<>();
		List <Layer> rootLayers = mapDefinition.getRootLayers();
		for (Layer rootLayer : rootLayers){
			final Traits<LayerType> layerType = layerTypeRegistry.getLayerType (rootLayer);
			if(layerType.has(TOCLayerTypeTrait.class)){
				tocItems.addAll(layerType.trait(TOCLayerTypeTrait.class).getTOC(layerType, rootLayer));
			}
		}
		
		List<Traits<TOCItem>> parentList = new ArrayList<>();
		return Collections.unmodifiableList(revertItems(tocItems, parentList));
		
	}
	

	private List<Traits<TOCItem>> revertItems (List<Traits<TOCItem>> tocItems, List<Traits<TOCItem>> parentList) {
		for(int n = tocItems.size() - 1; n >= 0; n--){
			Traits<TOCItem> tocItem = tocItems.get(n);
			List<Traits<TOCItem>> tocChildItems = tocItem.get().getItems();
			if(tocChildItems.size() > 0) {
				
				revertItems(tocChildItems,new ArrayList<Traits<TOCItem>>());
			}
			parentList.add(tocItem);	
		}
		return parentList;
	}
}
