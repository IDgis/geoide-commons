package controllers.mapview;

import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

public class Symbol extends Controller {
	
	private final MapProvider mapProvider;
	private final ServiceTypeRegistry serviceTypeRegistry;
	
	public Symbol (final MapProvider mapProvider, final ServiceTypeRegistry serviceTypeRegistry) {
		this.mapProvider = mapProvider;
		this.serviceTypeRegistry = serviceTypeRegistry;
	}

	public Promise<Result> legendSymbol (final String serviceLayerId) {
		final ServiceLayer serviceLayer = mapProvider.getServiceLayer (serviceLayerId);
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