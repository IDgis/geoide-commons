package nl.idgis.geoide.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.commons.layer.toc.TOCLayerTypeTrait;

public class DefaultTableOfContents implements TableOfContents {

	private final LayerTypeRegistry layerTypeRegistry;
	
	public DefaultTableOfContents (final LayerTypeRegistry layerTypeRegistry) {
		this.layerTypeRegistry = layerTypeRegistry;
	}
	
	@Override
	public CompletableFuture<List<Traits<TOCItem>>> getItems (final MapDefinition mapDefinition) {
		List<Traits<TOCItem>> tocItems = new ArrayList<>();
		List <Layer> rootLayers = mapDefinition.getRootLayers();
		for (Layer rootLayer : rootLayers){
			final Traits<LayerType> layerType = layerTypeRegistry.getLayerType (rootLayer);
			if(layerType.has(TOCLayerTypeTrait.class)){
				tocItems.addAll(layerType.trait(TOCLayerTypeTrait.class).getTOC(layerType, rootLayer));
			}
		}
		
		List<Traits<TOCItem>> parentList = new ArrayList<>();
		return CompletableFuture.completedFuture (Collections.unmodifiableList(revertItems(tocItems, parentList)));
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