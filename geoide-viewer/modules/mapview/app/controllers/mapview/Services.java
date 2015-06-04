package controllers.mapview;

import java.util.Map;

import javax.inject.Inject;

import nl.idgis.geoide.commons.domain.api.ServiceProviderApi;
import nl.idgis.geoide.commons.domain.service.messages.ServiceError;
import nl.idgis.geoide.commons.domain.service.messages.ServiceRequest;
import nl.idgis.geoide.commons.domain.service.messages.ServiceResponse;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

public class Services extends Controller {

	private final ServiceProviderApi serviceProvider;
	private final ActorRef serviceManager;
	
	@Inject
	public Services (final ServiceProviderApi serviceProvider, final ActorRef serviceManager) {
		this.serviceProvider = serviceProvider;
		this.serviceManager = serviceManager;
	}
	
	public Result serviceRequest (final String serviceId) {
		return ok ();
	}
	
	public Promise<Result> serviceRequestWithLayer (final String serviceId, final String layerName, final String path) {
		return Promises.asPromise (serviceProvider.findService (serviceId)).flatMap ((serviceIdentification) -> {
			if (serviceIdentification == null) {
				return Promise.pure ((Result) notFound ("Service not found"));
			}
			
			final ServiceRequest request = new ServiceRequest (
					serviceIdentification, 
					layerName, 
					path
				);
			
			return Promise.wrap (Patterns.ask (serviceManager, request, 10000)).map (new Function<Object, Result> () {
				@Override
				public Result apply (final Object result) throws Throwable {
					if (result instanceof ServiceError) {
						return reportServiceError ((ServiceError) result);
					} else if (result instanceof ServiceResponse) {
						final ServiceResponse response = (ServiceResponse) result;
						
						for (final Map.Entry<String, String> cacheHeader: response.cacheHeaders ().entrySet ()) {
							response ().setHeader (cacheHeader.getKey (), cacheHeader.getValue ());
						}
						
						response ().setContentType (response.contentType ());
						
						return ok (response.data ().toArray ());
					} else {
						throw new IllegalArgumentException ("Unknown message type: %" + result.getClass ().getCanonicalName ());
					}
				}
			});
		});
	}
	
	private static Result reportServiceError (final ServiceError serviceError) {
		switch (serviceError.errorType ()) {
		case EXCEPTION:
			return internalServerError (serviceError.message ());
		case INVALID_URL:
			return badRequest (serviceError.message ());
		case FORMAT_ERROR:
		case HTTP_ERROR:
		case SERVICE_ERROR:
			// Report bad gateway:
			return status (502, serviceError.message ());
		case TIMEOUT:
			// Report gateway timeout:
			return status (504, serviceError.message ());
		case UNSUPPORTED_OPERATION:
			return badRequest ();
		default:
			return internalServerError (serviceError.message ());
		}
	}
}
