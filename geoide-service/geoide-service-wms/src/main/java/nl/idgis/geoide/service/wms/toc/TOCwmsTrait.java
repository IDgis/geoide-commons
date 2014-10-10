package nl.idgis.geoide.service.wms.toc;

import java.util.ArrayList;
import java.util.List;

import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.commons.domain.toc.Symbol;
import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.service.toc.TOCServiceTypeTrait;


public class TOCwmsTrait implements TOCServiceTypeTrait{
	
	public List<Traits<TOCItem>>getTOC(final List<ServiceLayer> serviceLayers) {
		final List<Traits<TOCItem>> tocItems = new ArrayList<Traits<TOCItem>>();
		
		for (final ServiceLayer serviceLayer: serviceLayers) {
			List<Traits<TOCItem>> tocChildItems = new ArrayList<Traits<TOCItem>>();
			Traits<TOCItem> t = Traits.create(new TOCItem(tocChildItems,serviceLayer.getLabel(),false,true,new Symbol()));
			tocItems.add(t);
		}	
		return tocItems;
	}

	
}
