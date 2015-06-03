package nl.idgis.geoide.commons.domain.api;

import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.ServiceIdentification;

public interface ServiceProviderApi {

	CompletableFuture<ServiceIdentification> findService (String serviceId);
}
