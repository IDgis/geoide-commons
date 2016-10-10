package nl.idgis.geoide.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.LayerRef;
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
		List <LayerRef> rootLayers = mapDefinition.getRootLayers();
		for (LayerRef rootLayer : rootLayers){
			final Traits<LayerType> layerType = layerTypeRegistry.getLayerType (rootLayer.getLayer());
			if(layerType.has(TOCLayerTypeTrait.class)){
				tocItems.addAll(layerType.trait(TOCLayerTypeTrait.class).getTOC(layerType, rootLayer));
			}
		}
		return CompletableFuture.completedFuture (Collections.unmodifiableList(reverse(tocItems)));
	}

	

	private List<Traits<TOCItem>> reverse(List<Traits<TOCItem>> tocItems) {
	    if(tocItems.size() > 0) {                   
	    	Traits<TOCItem> value = tocItems.remove(0);
	    	List<Traits<TOCItem>> tocChildItems = value.get().getItems();
	    	if(tocChildItems.size() > 0) {
	    		 reverse(tocChildItems);
	    	}
	        reverse(tocItems);
	        tocItems.add((Traits<TOCItem>) value);
	    }
	    return tocItems;
	}
	
}
