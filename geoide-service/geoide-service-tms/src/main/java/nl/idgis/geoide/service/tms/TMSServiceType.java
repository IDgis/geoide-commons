package nl.idgis.geoide.service.tms;

import java.io.Serializable;
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
import nl.idgis.geoide.service.tms.actors.TMS;
import play.libs.ws.WSClient;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	public List<JsonNode>  getLayerRequestUrls (ServiceRequest serviceRequest, JsonNode mapExtent, double resolution, int outputWidth, int outputHeight ) {
		final String serviceEndPoint = serviceRequest.getService().getIdentification().getServiceEndpoint();
		final String serviceVersion = serviceRequest.getService().getIdentification().getServiceVersion();
		final TMSRequestParameters parameters = (TMSRequestParameters) serviceRequest.getParameters();
		final TilingProperties props = new TilingProperties();
		final int tilingLevel = props.getTilingLevel(resolution).getKey();
		final double tilingResolution = props.getTilingLevel(resolution).getValue();
		//TODO: get href from capabilities
		//final String baseUrl = serviceEndPoint + "/" + parameters.getLayer() + "/" + serviceVersion + "/" + tilingLevel;
		final String baseUrl = serviceEndPoint + "/" + tilingLevel;
		return  getTileCoördinates (props, mapExtent, tilingResolution, baseUrl);
		
		//Tiling richtlijn 
		/**
		 * 	0 3440,640 
			1 1720,320
			2 860,160 
			3 430,080
			4 215,040 
			5 107,520 
			6 53,760 
			7 26,880 
			8 13,440
			9 6,720 
			10 3,360
			11 1,680
			12 0,840 
			13 0,420 
			14 0,210
			**/
		
	}
	
	
	private List<JsonNode> getTileCoördinates (TilingProperties props, JsonNode mapExtent, double resolution, String baseUrl) {
		final int  tileWidth = props.getTileWidth();
		final int  tileHeight = props.getTileHeight();
		final double llXExtent = mapExtent.path("minx").asDouble();
		final double ulYExtent = mapExtent.path("maxy").asDouble();
		
		//final double llYExtent = mapExtent.path("miny").asDouble();
		int tileNrX = (int) Math.floor ((llXExtent-props.getLowerLeftX())/(resolution*tileWidth));
		int tileNrY = (int) Math.floor ((ulYExtent-props.getLowerLeftY() )/(resolution*tileHeight));
		//int llTileY = (int) Math.floor (((llYExtent-props.getLowerLeftY())/resolution)/tileHeight);
		double startTileXm  =  props.getLowerLeftX() + tileNrX * tileWidth * resolution;
		double startTileYm  =  props.getLowerLeftY() + tileNrY * tileHeight * resolution;
		int llposX = (int) ((startTileXm - llXExtent) / resolution);
		int ulPosY = (int) ((ulYExtent - startTileYm) / resolution) - tileHeight ;
		ObjectMapper mapper = new ObjectMapper();
		List<JsonNode> tileRequests = new ArrayList<JsonNode>();
		double tileXm = startTileXm;
		int tileX = tileNrX; 
		int posX = llposX;
		while( tileXm  <  mapExtent.path("maxx").asDouble() ) {
			double tileYm = startTileYm;
			int tileY = tileNrY; 
			int posY = ulPosY;
			while( tileYm + (resolution * tileHeight) >  mapExtent.path("miny").asDouble()) {
				ObjectNode tileRequest = mapper.createObjectNode();
				tileRequest.put("uri", baseUrl + "/" + tileX + "/" + tileY + ".png");
				tileRequest.put("left", posX);
				tileRequest.put("right", posX + tileWidth);
				tileRequest.put("top", posY);
				tileRequest.put("bottom", posY + tileHeight);
				tileRequest.put("resolution", resolution);
				tileRequests.add(tileRequest);
				tileY -= 1;
				posY += tileHeight;
				tileYm -= tileHeight * resolution;
				if(tileRequests.size() > 15*15) {
					//TODO throw error
				}
			}
			
			tileX += 1;
			posX += tileWidth;
			tileXm += tileWidth *resolution;
		}
		
		return tileRequests;
		
		
		
	}
	

	@Override
	public Props createServiceActorProps (final ActorRef serviceManager, final WSClient wsClient, final ServiceIdentification identification) {
		return TMS.mkProps (serviceManager, wsClient, identification);
	}
	

	public static class TMSRequestParameters implements Serializable {
		private static final long serialVersionUID = 5824460737314719140L;
		
		private final String layer;
		
		public TMSRequestParameters (final String layer) {
			this.layer = layer;
		}

		public String getLayer () {
			return layer;
		}
	}
	
	
	public static class TilingProperties {
		
		//TODO getTilingProperties from Capabilities
		private final double llX = -285401.920;
		private final double llY = 22598.080;
		private final int tileWidth = 256;
		private final int tileHeight = 256;
		private final Map<Integer, Double> levels = new HashMap<Integer, Double>();
		
		public TilingProperties () {
			levels.put(0, 3440.640);
		 	levels.put( 1, 1720.320);
		 	levels.put( 2, 860.160); 
			levels.put( 3, 430.080);
			levels.put( 4, 215.040); 
			levels.put( 5, 107.520); 
			levels.put( 6, 53.760); 
			levels.put( 7, 26.880); 
			levels.put( 8, 13.440);
			levels.put( 9, 6.720); 
			levels.put( 10, 3.360);
			levels.put( 11, 1.680);
			levels.put( 12, 0.840); 
			levels.put( 13, 0.420); 
			levels.put( 14, 0.210);
		}

		public  Map.Entry<Integer, Double> getTilingLevel (double resolution) {
			for (Map.Entry<Integer, Double> entry : levels.entrySet()){
				if(resolution < entry.getValue()) {
					continue;	
				}
				return entry;
			}
			return null;
		}
		
		public double getLowerLeftX(){
			return llX;
		}
		
		public double getLowerLeftY(){
			return llY;
		}
		
		public int getTileWidth(){
			return tileWidth;
		}
		
		public int getTileHeight(){
			return tileHeight;
		}
	}
	
}
