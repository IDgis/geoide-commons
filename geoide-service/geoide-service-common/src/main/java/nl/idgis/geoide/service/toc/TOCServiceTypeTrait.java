package nl.idgis.geoide.service.toc;

import java.util.List;

import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.service.ServiceTypeTrait;


public interface TOCServiceTypeTrait extends ServiceTypeTrait {
	public List<Traits<TOCItem>> getTOC();
}
