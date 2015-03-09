package nl.idgis.geoide.service.wms;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceRequestContext;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.wms.actors.WMS;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WMSServiceType extends ServiceType implements LayerServiceType {
	private final static Set<String> versions;
	static {
		final HashSet<String> v = new HashSet<> ();
		v.add ("1.1.1");
		v.add ("1.3.0");
		versions = Collections.unmodifiableSet (v);
	}
	
	@Override
	public String getTypeName () {
		return "WMS";
	}

	@Override
	public Set<String> getSupportedVersions () {
		return versions;
	}

	@Override
	public String normalizeEndpoint (final String endpoint) {
		final String endpointWithProtocol = endpoint.contains ("://") ? endpoint : "http://" + endpoint;
		
		if (endpointWithProtocol.contains ("?")) {
			if (!endpointWithProtocol.endsWith ("?")) {
				return endpointWithProtocol + "&";
			} else {
				return endpointWithProtocol;
			}
		} else {
			return endpointWithProtocol + "?";
		}
	}

	@Override
	public List<ServiceRequest> getServiceRequests (final Service service, final List<ParameterizedServiceLayer<?>> serviceLayers, final ServiceRequestContext context) {
		final List<ServiceRequest> serviceRequests = new ArrayList<ServiceRequest> ();
		final StringBuilder layers = new StringBuilder ();
		Map<String, String> currentVendorParameters = Collections.<String, String>emptyMap ();
		
		for (final ParameterizedServiceLayer<?> serviceLayer: serviceLayers) {
			final WMSLayerParameters parameters = 
					serviceLayer.getParameters() != null && serviceLayer.getParameters () instanceof WMSLayerParameters
					? (WMSLayerParameters)serviceLayer.getParameters ()
					: new WMSLayerParameters ();
			final Map<String, String> vendorParameters = parameters.getVendorParameters ();

			// Flush the layers if the vendor parameters change or if this is a "single request" layer:
			if (layers.length () > 0 && (parameters.isSingleRequest () || !vendorParameters.equals (currentVendorParameters))) {
				serviceRequests.add (new ServiceRequest (
						context.nextServiceIdentifier (service, null),
						service,
						new WMSRequestParameters (layers.toString (), true, currentVendorParameters)
					));
				layers.setLength (0);
			}

			// Add the layer:
			if (layers.length () > 0) {
				layers.append (",");
			}
			layers.append (serviceLayer.getServiceLayer ().getName ().getLocalName ());
			
			// Store the current vendor parameters:
			currentVendorParameters = vendorParameters;

			// Emit a single request if this is a single request layer:
			if (parameters.isSingleRequest ()) {
				serviceRequests.add (new ServiceRequest (
						context.nextServiceIdentifier (service, null),
						service,
						new WMSRequestParameters (layers.toString (), true, currentVendorParameters)
					));
				layers.setLength (0);
			}
		}

		if (layers.length () > 0) {
			serviceRequests.add (new ServiceRequest (
					context.nextServiceIdentifier (service, null), 
					service, 
					new WMSRequestParameters (layers.toString (), true, currentVendorParameters)
				));
		}
		
		return serviceRequests;
	}
	
	
	@Override
	public List<JsonNode> getLayerRequestUrls (ServiceRequest serviceRequest, JsonNode mapExtent, double resolution, int outputWidth, int outputHeight ) {
		
		String serviceEndPoint = serviceRequest.getService().getIdentification().getServiceEndpoint();
		String serviceVersion = serviceRequest.getService().getIdentification().getServiceVersion();
		
		WMSRequestParameters parameters = (WMSRequestParameters) serviceRequest.getParameters();		
		String vendorParamString = "&";
	
		Map<String, String> vendorParameters = parameters.getVendorParameters();
		if(!vendorParameters.isEmpty()){
			for (Map.Entry<String, String> vParam : vendorParameters.entrySet()){
				vendorParamString += vParam.getKey() + "=" + vParam.getValue();
			}
		}
		//TODO outputformat
		
		String bbox = mapExtent.path("minx") + "," +  mapExtent.path("miny") + "," +
						mapExtent.path("maxx") + "," +  mapExtent.path("maxy");
		
		String requestUrl = serviceEndPoint + "?SERVICE=WMS&VERSION=" + serviceVersion +  "&REQUEST=GetMap&FORMAT=image%2Fsvg%2Bxml" +
			"&layers="  + parameters.getLayers() + "&transparent=" + parameters.getTransparent() + "&CRS=EPSG%3A28992&STYLES=" +
			"&BBOX=" + bbox + "&WIDTH=" + outputWidth + "&HEIGHT=" + outputHeight + vendorParamString.replaceAll(" ", "%20");
		
		ObjectMapper mapper = new ObjectMapper();
		List<JsonNode> requests = new ArrayList<JsonNode>();
		ObjectNode request = mapper.createObjectNode();
		request.put("uri", requestUrl);
		request.put("left", 0);
		request.put("right", outputWidth);
		request.put("top", 0);
		request.put("bottom", outputHeight);
		
		requests.add(request);
		
		return requests;
	}
	
	
	@Override
	public Props createServiceActorProps (final ActorRef serviceManager, final ServiceIdentification identification) {
		return WMS.mkProps (serviceManager, identification);
	}
	
	@JsonInclude (Include.NON_NULL)
	public static class WMSRequestParameters {
		
		private final String layers;
		private final Boolean transparent;
		private final Map<String, String> vendorParameters;
		
		public WMSRequestParameters (final String layers, final Boolean transparent) {
			this (layers, transparent, null);
		}
		
		public WMSRequestParameters (final String layers, final Boolean transparent, final Map<String, String> vendorParameters) {
			this.layers = layers;
			this.transparent = transparent;
			this.vendorParameters = vendorParameters != null ? new HashMap<String, String> (vendorParameters) : Collections.<String, String>emptyMap ();
		}
		
		public String getLayers () {
			return layers;
		}
		
		public Boolean getTransparent () {
			return transparent;
		}
		
		@JsonAnyGetter
		public Map<String, String> getVendorParameters () {
			return Collections.unmodifiableMap (vendorParameters);
		}
	}
}
