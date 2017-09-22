package nl.idgis.geoide.service.tms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.service.TMSRequestParameters;

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
	private static final Set<String> versions;
	private static Logger log = LoggerFactory.getLogger(TMSServiceType.class);

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
		final List<ServiceRequest> serviceRequests = new ArrayList ();
		
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
		final TilingProperties props = new TilingProperties(serviceEndPoint);	
		return  getTileCoordinates (props, mapExtent, resolution);
		
	}
	
	
	private List<JsonNode> getTileCoordinates (TilingProperties props, JsonNode mapExtent, double resolution) {
		
		final int  tileWidth = props.getTileWidth();
		final int  tileHeight = props.getTileHeight();
		final double llXExtent = mapExtent.path("minx").asDouble();
		final double ulYExtent = mapExtent.path("maxy").asDouble();
		
		Map<Double, String> levels = props.getTileSets();
		SortedSet<Double> keys = new TreeSet<Double>(Collections.reverseOrder());
		keys.addAll(levels.keySet());
		
		String href = null;
		
		for(Double entry : keys) {
			if(resolution < entry) {
				continue;	
			}
			
			href = levels.get(entry);
			resolution = entry;
			
			break;
		}
		
		final String ext = props.getExtension(); 
		
		int tileNrX = (int) Math.floor ((llXExtent-props.getLowerLeftX())/(resolution*tileWidth));
		int tileNrY = (int) Math.floor ((ulYExtent-props.getLowerLeftY() )/(resolution*tileHeight));
		double startTileXm  =  props.getLowerLeftX() + tileNrX * tileWidth * resolution;
		double startTileYm  =  props.getLowerLeftY() + tileNrY * tileHeight * resolution;
		int llposX = (int) ((startTileXm - llXExtent) / resolution);
		int ulPosY = (int) ((ulYExtent - startTileYm) / resolution) - tileHeight ;
		
		ObjectMapper mapper = new ObjectMapper();
		List<JsonNode> tileRequests = new ArrayList<JsonNode>();
		double tileXm = startTileXm;
		int tileX = tileNrX; 
		int posX = llposX;
		
		if(href != null) {
			while( tileXm  <  mapExtent.path("maxx").asDouble() ) {
				double tileYm = startTileYm;
				int tileY = tileNrY; 
				int posY = ulPosY;
				while( tileYm + (resolution * tileHeight) >  mapExtent.path("miny").asDouble()) {
					ObjectNode tileRequest = mapper.createObjectNode();
					
					tileRequest.put("uri", href + "/" + tileX + "/" + tileY + "." + ext);
					tileRequest.put("left", posX);
					tileRequest.put("right", posX + tileWidth);
					tileRequest.put("top", posY);
					tileRequest.put("bottom", posY + tileHeight);
					tileRequest.put("resolution", resolution);
					tileRequests.add(tileRequest);
					tileY -= 1;
					posY += tileHeight;
					tileYm -= tileHeight * resolution;
					if (tileRequests.size() > 15*15) {
						log.info("Large number of tiles: {}", tileRequests.size());
					}
				}
				
				tileX += 1;
				posX += tileWidth;
				tileXm += tileWidth *resolution;
			}
		} else {
			log.debug("href is null");
			log.debug("tilesets: " + props.getTileSets());
			log.debug("resolution: " + resolution);
			
		}
		
		return tileRequests;
	}
	
	@Override
	public Props createServiceActorProps (final ActorRef serviceManager, final WSClient wsClient, final ServiceIdentification identification) {
		return TMS.mkProps (serviceManager, wsClient, identification);
	}
	

	
	public class TilingProperties {
		//
		private double llX = -285401.92;
		private double llY = 22598.08;
		private int tileWidth = 256;
		private int tileHeight= 256;
		private String extension = "png";
		private final Map<Double, String> tileSets = new HashMap();
			
		public TilingProperties (String serviceEndPoint){
			InputStream input = null;
			try {
				input = new URL(serviceEndPoint).openStream();
			} catch (IOException e2) {
				log.error("Error while reading TMS Capabilities from {} {}", serviceEndPoint, e2);
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e1) {
				log.error("Error in parsing TMS Capabilities from {} {}", serviceEndPoint, e1);
			}
		    Document doc = null;
			try {
				if(builder != null) {
					doc = builder.parse(input);
				}
			} catch (SAXException | IOException e) {
				log.error("Error in parsing TMS Capabilities from {} {}", serviceEndPoint, e);
			}
			
			if( doc!= null){
				readCapabilitiesFromDoc (doc, serviceEndPoint);
			}
		}
		
		private void readCapabilitiesFromDoc(Document doc, String serviceEndPoint) {
			
			if (doc.getElementsByTagName("TileFormat").getLength() != 0) {
				Element  tileFormat = (Element) doc.getElementsByTagName("TileFormat").item(0);
				extension = tileFormat.getAttribute("extension");
				if (extension.isEmpty()) {
					log.info("TMS Capabilities document from {}, is missing TileFormat extension attribute, default 'png' used.", serviceEndPoint);
				}
				if (tileFormat.getAttribute("width").isEmpty() || tileFormat.getAttribute("height").isEmpty()) {
					log.info("TMS Capabilities document from {}, is missing Tile size attributes, default 256 used.", serviceEndPoint);
				} else {
					tileWidth = Integer.parseInt(tileFormat.getAttribute("width"));
					tileHeight =  Integer.parseInt(tileFormat.getAttribute("height"));
				}		
			} else {
				log.info("TMS Capabilities document from {}, is missing TileFormat tag, defaults used.", serviceEndPoint);
			}
			
			if (doc.getElementsByTagName("Origin").getLength() != 0) {
				Element origin = (Element) doc.getElementsByTagName("Origin").item(0);
				if (origin.getAttribute("x").isEmpty() || origin.getAttribute("y").isEmpty()) {
					log.info("TMS Capabilities document from {}, is missing Tile origin attributes, defaults used.", serviceEndPoint);
				} else {
					llX = Double.parseDouble(origin.getAttribute("x"));
					llY = Double.parseDouble(origin.getAttribute("y"));
				}	
			} else {
				log.info("TMS Capabilities document from {}, is missing Origin tag, defaults used.", serviceEndPoint);
			}
			
			NodeList sets = doc.getElementsByTagName("TileSet");
			buildTileSets (sets, serviceEndPoint);
			
		}
		
		private void buildTileSets(NodeList sets, String serviceEndPoint) {
			if (sets.getLength() != 0) { 
				for (int i = 0; i < sets.getLength(); i++){
					Element set = (Element) sets.item(i);
					String href = set.getAttribute("href");
					if (href.isEmpty() || set.getAttribute("units-per-pixel").isEmpty()){
						log.error("TMS Capabilities document from {}, is missing TileSet href or units-per-pixel!", serviceEndPoint);
					} else {
						Double resolution = Double.parseDouble(set.getAttribute("units-per-pixel"));
						tileSets.put(resolution,  href);
					}
				}
			} else {
				log.error("TMS Capabilities document from {}, is missing TileSets!", serviceEndPoint);
			}
		}
		
		public  Map<Double, String> getTileSets () {
			return tileSets;
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
		public String getExtension(){
			return extension;
		}
	}
	
}
