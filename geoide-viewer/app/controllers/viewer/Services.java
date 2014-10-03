package controllers.viewer;

import java.util.Map;

import nl.idgis.planoview.commons.domain.Service;
import nl.idgis.planoview.commons.domain.provider.MapProvider;
import nl.idgis.planoview.service.messages.ServiceError;
import nl.idgis.planoview.service.messages.ServiceRequest;
import nl.idgis.planoview.service.messages.ServiceResponse;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

public class Services extends Controller {

	private final MapProvider mapProvider;
	private final ActorRef serviceManager;
	
	public Services (final MapProvider mapProvider, final ActorRef serviceManager) {
		this.mapProvider = mapProvider;
		this.serviceManager = serviceManager;
	}
	
	public Result serviceRequest (final String serviceId) {
		return ok ();
	}
	
	public Promise<Result> serviceRequestWithLayer (final String serviceId, final String layerName, final String path) {
		final Service service = mapProvider.getService (serviceId);
		if (service == null) {
			return Promise.pure ((Result) notFound ("Service not found"));
		}
		
		final ServiceRequest request = new ServiceRequest (
				service.getIdentification (), 
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
