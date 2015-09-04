package nl.idgis.geoide.service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import akka.actor.ActorRefFactory;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.api.ServiceManager;
import nl.idgis.geoide.commons.domain.service.Capabilities;
import nl.idgis.geoide.commons.domain.service.QueryFeaturesResponse;
import nl.idgis.geoide.commons.domain.service.ServiceResponse;
import nl.idgis.geoide.commons.http.client.HttpClient;

public class DefaultServiceManager implements ServiceManager {

	private final ActorRefFactory actorRefFactory;
	private final HttpClient httpClient;
	
	public DefaultServiceManager (final ActorRefFactory actorRefFactory, final HttpClient httpClient) {
		this.actorRefFactory = Objects.requireNonNull (actorRefFactory, "actorRefFactory cannot be null");
		this.httpClient = Objects.requireNonNull (httpClient, "httpClient cannot be null");
	}
	
	@Override
	public CompletableFuture<Capabilities> getCapabilities (final ServiceIdentification serviceIdentification) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<QueryFeaturesResponse> queryFeatures (final ServiceIdentification serviceIdentification,
			final ParameterizedFeatureType<?> featureType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<ServiceResponse> serviceRequest (final ServiceIdentification serviceIdentification,
			final String layerName, 
			final String path) {
		// TODO Auto-generated method stub
		return null;
	}

}
