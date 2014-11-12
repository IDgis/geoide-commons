package nl.idgis.geoide.commons.layer.toc;

import java.util.ArrayList;
import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.commons.domain.toc.Symbol;
import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.service.toc.TOCServiceTypeTrait;

public class TOCdefaultLayerTypeTrait implements TOCLayerTypeTrait {
	private final ServiceTypeRegistry serviceTypeRegistry; 
	
	public TOCdefaultLayerTypeTrait(ServiceTypeRegistry serviceTypeRegistry) {
		this.serviceTypeRegistry = serviceTypeRegistry;
	}

	@Override
	public List<Traits<TOCItem>> getTOC(Traits<LayerType> layerType, Layer layer) {
		List<ServiceLayer>  serviceLayers = layer.getServiceLayers();
		List<Traits<TOCItem>> tocChildItems = new ArrayList<>();
		for( ServiceLayer serviceLayer: serviceLayers) {
			String serviceTypeName = serviceLayer.getService().getIdentification().getServiceType();
			Traits<ServiceType> serviceType = serviceTypeRegistry.getServiceType(serviceTypeName);
			if (serviceType.has(TOCServiceTypeTrait.class)){
				tocChildItems.addAll(serviceType.trait(TOCServiceTypeTrait.class).getTOC(serviceType,serviceLayer));
			}	
		}
		List<Layer> sublayers = layer.getLayers();
		for (Layer sublayer: sublayers) {
			tocChildItems.addAll(getTOC(layerType, sublayer));
		}
		
		Traits<TOCItem> tocItem = Traits.create (TOCItem
				.builder ()
				.setItems (tocChildItems)
				.setLabel (layer.getLabel ())
				.setActivatable (true)
				.setActive (false)
				.setExpandable (true)
				.setExpanded (false)
				.build ()
			);
		
		tocItem = tocItem.with(new TOCItemLayerTrait(layer));
		
		
		List<Traits<TOCItem>> tocItems = new ArrayList<>();
		tocItems.add(tocItem);
		return tocItems;
	}
	

}
