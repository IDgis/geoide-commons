package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.report.ReportData;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.map.MapView;
import nl.idgis.geoide.map.MapView.LayerWithState;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.ogc.util.MimeContentType;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;

public class MapBlockComposer implements BlockComposer {
	final MapView mapView;
	final DocumentCache documentCache;
	private URI mapCssUri;
	
	
	/**
	 * A specialized composer component that composes a mapBlock with information from the client 
	 * and a (part of) a report template
	 **/ 
	
	public MapBlockComposer(MapView mapView, DocumentCache documentCache) {
		super();
		this.mapView = mapView;
		this.documentCache= documentCache;
	}

	@Override
	public Promise<Element> compose(JsonNode blockInfo, Element block, ReportData reportData) throws Throwable {
		
		final JsonNode viewerstate = blockInfo.get("viewerstate");
		final double resolution = viewerstate.path("resolution").asDouble();
		
		double gridWidth= Integer.parseInt(block.attr("data-grid-width"));
		double blockWidthmm = reportData.getReportWidth() * gridWidth/12; 
		
		//van mm naar px
		double blockWidthpx  = blockWidthmm / 0.28;
		double mapWidthm = getMapWidthm(viewerstate);
		double mapHeigthm = getMapHeightm(viewerstate);
		double resizeFactor = resolution/(mapWidthm/blockWidthpx);
		double blockHeightpx = (blockWidthpx/mapWidthm * mapHeigthm); 

		
		final List<LayerWithState> layers = mapView.flattenLayerList (viewerstate);
		final List<ServiceRequest> serviceRequests = mapView.getServiceRequests (layers);
		
		Element mapRow = block.appendElement("div");
		mapRow.attr("class", "map_row");
		
		String mapCss = ".map_row {" +
			    "height: " +(int) blockHeightpx + "px;" + 
				"width: " + (int) blockWidthpx + "px;" +
			    "position: relative;" +
				"border: 1px solid gray;" + 
			"}";
		
		int layernr = 1;
		//loop over serviceRequest en bouw urls
		for (final ServiceRequest request: serviceRequests) {
			Element mapLayer = mapRow.appendElement("div");
			mapLayer.attr("id", "map_layer" + layernr);
			mapCss += getLayerCss(layernr, blockHeightpx, blockWidthpx);
			
			
			ServiceType serviceType = mapView.getServiceType(request.getService());
			
			if (serviceType instanceof LayerServiceType ) {
				int reqnr = 1;
				
				
				LayerServiceType layerServiceType = (LayerServiceType) serviceType;
				List<JsonNode>  requestUrls = layerServiceType.getLayerRequestUrls(request, viewerstate.path("extent"), resolution,(int) blockWidthpx, (int) blockHeightpx);
				for (JsonNode requestUrl:requestUrls) {
					Element reqElem; 
					String referenceY = "top";
					String reqId = "map_layer" + layernr + "_" + reqnr;
					if (request.getService().getIdentification().getServiceType().equals("TMS")){
						reqElem = mapLayer.appendElement("img");
						reqElem.attr("src", requestUrl.path("uri").asText());
						referenceY = "bottom";
					} else {
						reqElem = mapLayer.appendElement("object");
						reqElem.attr("type", "image/svg+xml");
						reqElem.attr("data", requestUrl.path("uri").asText());
					}	
					reqElem.attr("id", reqId);
					
					
					int cssWidth;
					int cssHeight;
					if (request.getService().getIdentification().getServiceType().equals("TMS")){
						cssWidth = (int) (resizeFactor * 256);
						cssHeight = cssWidth;
					} else {
						cssWidth = (int) blockWidthpx;
						cssHeight = (int) blockHeightpx;
					}
					
					mapCss += getReqCss (reqId, cssWidth, cssHeight,  (int) (requestUrl.path("posX").asInt() * resizeFactor), (int) (requestUrl.path("posY").asInt()*resizeFactor), referenceY, (int) blockWidthpx, (int) blockHeightpx  );
					reqnr += 1;
				}	
			}
			
			layernr += 1;
		}
				
		
		//http://localhost:8080/crs-deegree-webservices/services/crs2_wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fsvg%2Bxml&TRANSPARENT=true&layers=afdelingen_test&transparent=true&CRS=EPSG%3A28992&STYLES=&WIDTH=1197&HEIGHT=897&BBOX=203578.790717474%2C373499.5971701656%2C204584.270717474%2C374253.0771701656
			
		//serviceEndpoint + "?SERVICE=" + serviceType + "&VERSION=" + serviceVersion +  "&REQUEST=GetMap&FORMAT=image%2Fsvg%2Bxml" + parameters + "&CRS=EPSG%3A28992&STYLES=&WIDTH=1197&HEIGHT=897&BBOX=203578.790717474%2C373499.5971701656%2C204584.270717474%2C374253.0771701656" 
		System.out.println("***************" + mapCss);	
		
		mapCssUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		documentCache.store(mapCssUri, new MimeContentType ("text/css"), mapCss.getBytes());
		
		return Promise.pure(block);


	};

	private String getLayerCss(int layernr, double blockHeightpx, double blockWidthpx) {
		 return "#map_layer"+ layernr + " {" +
			    "position: absolute;" +
			    "z-index: " + layernr + ";" +
			    "left: 0px;" +
			    "top: 0px;" + 
			    "height: " +(int) blockHeightpx + "px;" + 
				"width: " + (int) blockWidthpx + "px;" +
			"}";
	}
	private String getReqCss(String id, int width, int height, int posx, int posy, String referenceY, int blockWidth, int blockHeight ) {
		
		//default clip properties
		int t = 0;
		int r = width;
		int b = height;
		int l = 0;
		
		if (posx < 0) {
			l = Math.abs(posx);
		}
		if (posy < 0) {
			b = height - Math.abs(posy);
		}
		if (posx + width >  blockWidth) {
			r = blockWidth - posx;
		}
		if (posy + height > blockHeight) {
			t = height- (blockHeight - posy);
		}
		
		return "#" + id + " {" +
			    "width:" + width + "px;" +
		 		"height:" + height + "px;" +
		 		"position: absolute;" +
		 		"left: " + posx + "px;" +
		 		referenceY + ":" + posy + "px;" +
		 		"clip: rect(" + t + "px," + r + "px," + b + "px," + l + "px);" + 
		 		"}";
	}
	

	private int getMapHeightm(JsonNode viewerstate) {
		JsonNode extentNode = viewerstate.path("extent");
		if(extentNode!=null){
			 return (extentNode.path("maxy")).intValue() - (extentNode.path("miny")).intValue();
		}
		JsonNode scaleNode = viewerstate.path("scale");
		if(scaleNode!=null){
			//TODO
		}
		
		//TODO throw Error
		return 0;
	}

	private int getMapWidthm(JsonNode blockInfo) {
		JsonNode extentNode = blockInfo.path("extent");
		if(extentNode!=null){
			 return (extentNode.path("maxx")).intValue() - (extentNode.path("minx")).intValue();
		}
		JsonNode scaleNode = blockInfo.path("scale");
		if(scaleNode!=null){
			//TODO
		}
		
		//TODO throw Error
		return 0;
	}

	public URI getBlockCssUri() {
		return mapCssUri;
	}


	

}
