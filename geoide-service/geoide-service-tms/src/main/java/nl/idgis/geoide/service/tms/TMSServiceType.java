package nl.idgis.geoide.service.tms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.Props;
import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceRequestContext;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.tms.actors.TMS;

public class TMSServiceType extends ServiceType implements LayerServiceType {

	private final static Set<String> versions;
	static {
		final HashSet<String> v = new HashSet<> ();
		v.add ("1.0.0");
		versions = Collections.unmodifiableSet (v);
	}
	
	@Override
	public String getTypeName () {
		return "TMS";
	}

	@Override
	public Set<String> getSupportedVersions () {
		return versions;
	}

	@Override
	public String normalizeEndpoint (final String endpoint) {
		final String endpointWithProtocol = endpoint.contains ("://") ? endpoint : "http://" + endpoint;
		
		return endpointWithProtocol.endsWith ("/") ? endpointWithProtocol : endpointWithProtocol + "/";
	}

	@Override
	public List<ServiceRequest> getServiceRequests (final Service service, final List<ParameterizedServiceLayer<?>> serviceLayers, final ServiceRequestContext context) {
		final List<ServiceRequest> serviceRequests = new ArrayList<ServiceRequest> ();
		
		for (final ParameterizedServiceLayer<?> serviceLayer: serviceLayers) {
			final String layerName = serviceLayer.getServiceLayer ().getName ().getLocalName ();
			final String requestId = context.nextServiceIdentifier (service, layerName);
			
			serviceRequests.add (new ServiceRequest (requestId, service, new TMSRequestParameters (layerName)));
		}
		
		return serviceRequests;
	}

	@Override
	public Props createServiceActorProps (final ActorRef serviceManager, final ServiceIdentification identification) {
		return TMS.mkProps (serviceManager, identification);
	}
	
	
	public static class TMSRequestParameters {
		private final String layer;
		
		public TMSRequestParameters (final String layer) {
			this.layer = layer;
		}

		public String getLayer () {
			return layer;
		}
	}
}
