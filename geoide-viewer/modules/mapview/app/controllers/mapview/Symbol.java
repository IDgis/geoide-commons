package controllers.mapview;

import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.commons.domain.provider.ServiceLayerProvider;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

public class Symbol extends Controller {
	
	private final ServiceLayerProvider serviceLayerProvider;
	private final ServiceTypeRegistry serviceTypeRegistry;
	
	public Symbol (final ServiceLayerProvider serviceLayerProvider, final ServiceTypeRegistry serviceTypeRegistry) {
		this.serviceLayerProvider = serviceLayerProvider;
		this.serviceTypeRegistry = serviceTypeRegistry;
	}

	public Promise<Result> legendSymbol (final String serviceLayerId) {
		final ServiceLayer serviceLayer = serviceLayerProvider.getServiceLayer(serviceLayerId);
		if (serviceLayer == null) {
			return Promise.pure ((Result) notFound ("Layer " + serviceLayerId + " not found"));
		}
		
		final Service service = serviceLayer.getService ();
		final Traits<ServiceType> serviceType = serviceTypeRegistry.getServiceType (service.getIdentification().getServiceType ());
		
		if (serviceType == null) {
			throw new IllegalStateException ("Service type " + service.getIdentification ().getServiceType () + " not found"); 
		}
				
		return Promise.pure ((Result) ok ());
	}
}