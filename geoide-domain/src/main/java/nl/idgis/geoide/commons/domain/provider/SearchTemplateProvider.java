package nl.idgis.geoide.commons.domain.provider;
import nl.idgis.geoide.commons.domain.SearchTemplate;

public interface SearchTemplateProvider {
	SearchTemplate getSearchTemplate (String searchTemplateId);
}
