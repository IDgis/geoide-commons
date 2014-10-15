package nl.idgis.geoide.service.wms.toc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.commons.domain.toc.Symbol;
import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.toc.TOCServiceTypeTrait;


public class TOCwmsTrait implements TOCServiceTypeTrait{
	
	public TOCwmsTrait () {
		
	}
	
	@Override
	public List<Traits<TOCItem>>getTOC(final Traits<ServiceType> serviceType, final ServiceLayer serviceLayer) {
		
		final List<Traits<TOCItem>> tocItems = new ArrayList<>();
		
		tocItems.add(Traits.create (
			TOCItem
				.builder ()
				.setLabel (serviceLayer.getLabel ())
				.setSymbol (new Symbol ())
				.build ()
		));
			
		return Collections.unmodifiableList(tocItems);
	}

	
}
