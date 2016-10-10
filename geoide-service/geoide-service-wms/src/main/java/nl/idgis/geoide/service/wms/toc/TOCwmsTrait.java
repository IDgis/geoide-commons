package nl.idgis.geoide.service.wms.toc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
		String [] urlParts = serviceLayer.getLegendGraphicUrl().split("/");
		String legendGraphicUrl = "";
		if (urlParts.length > 0) {
			legendGraphicUrl = urlParts[0] + "//";
		}

		for (int n = 1; n < urlParts.length; n++) {
			try {
				legendGraphicUrl += URLEncoder.encode(urlParts[n],"UTF8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (n + 1 != urlParts.length){
				legendGraphicUrl += "/";
			}
		}
		tocItems.add(Traits.create (
			TOCItem
				.builder ()
				.setLabel (serviceLayer.getLabel ())
				.setSymbol (new Symbol (serviceLayer.getId(), legendGraphicUrl))
				.setIsGroup(false)
				.build ()
		));
		
			
		return Collections.unmodifiableList(tocItems);
	}

	
}
