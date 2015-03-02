package nl.idgis.geoide.commons.domain.provider;

import nl.idgis.geoide.commons.domain.Service;

public interface ServiceProvider {
	Service getService (String serviceId);
}
