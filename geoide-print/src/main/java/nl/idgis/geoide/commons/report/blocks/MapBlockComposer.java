package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.report.ReportData;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.map.MapView;
import nl.idgis.geoide.map.MapView.LayerWithState;
import nl.idgis.ogc.util.MimeContentType;

import org.jsoup.nodes.Element;

import play.libs.F.Promise;
import play.libs.Json;

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
		
		JsonNode viewerstate = blockInfo.get("viewerstate");
		
		double gridWidth= Integer.parseInt(block.attr("data-grid-width"));
		double blockWidthmm = reportData.getReportWidth() * gridWidth/12; 
		//van mm naar px
		double blockWidthpx  = blockWidthmm / 0.28;
		double mapWidthm = getMapWidthm(viewerstate);
		double mapHeigthm = getMapHeightm(viewerstate);
		double blockHeightpx = (blockWidthpx/mapWidthm * mapHeigthm); 
		
		final List<LayerWithState> layers = mapView.flattenLayerList(viewerstate);
		final List<ServiceRequest> serviceRequests = mapView.getServiceRequests (layers);
		
		String mapHtml = "<div class='map_row'>";
		
		String mapCss = ".map_row {" +
			    "height: " +(int) blockHeightpx + "px;" + 
				"width: " + (int) blockWidthpx + "px;" +
			    "position: relative;" +
				"border: 1px solid gray;" + 
			"}";
		
		int layernr = 1;
		//loop over serviceRequest en bouw urls
		for (final ServiceRequest request: serviceRequests) {
			mapHtml += "<div class='map_layer" + layernr +"'>";
			mapCss += getLayerCss(layernr);
			String requestUrl = "";
			if(request.getService().getIdentification().getServiceType().equals("WMS")) {
				requestUrl = getRequestUrl(request) + "&WIDTH=" + (int) blockWidthpx + "&HEIGHT=" + (int) blockHeightpx + "&BBOX=" + getBBox(viewerstate);				
				mapHtml += "<object id='object" + layernr  + "' type='image/svg+xml' data='" + requestUrl + "'></object>";
				mapCss += getObjectCss(layernr, (int)blockWidthpx, (int)blockHeightpx);
			};
			
			
			mapHtml += "</div>";
			
			layernr += 1;
		}
				
		mapHtml += "</div>";
		
		//http://localhost:8080/crs-deegree-webservices/services/crs2_wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fsvg%2Bxml&TRANSPARENT=true&layers=afdelingen_test&transparent=true&CRS=EPSG%3A28992&STYLES=&WIDTH=1197&HEIGHT=897&BBOX=203578.790717474%2C373499.5971701656%2C204584.270717474%2C374253.0771701656
			
		//serviceEndpoint + "?SERVICE=" + serviceType + "&VERSION=" + serviceVersion +  "&REQUEST=GetMap&FORMAT=image%2Fsvg%2Bxml" + parameters + "&CRS=EPSG%3A28992&STYLES=&WIDTH=1197&HEIGHT=897&BBOX=203578.790717474%2C373499.5971701656%2C204584.270717474%2C374253.0771701656" 
			
		
		mapCssUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		documentCache.store(mapCssUri, new MimeContentType ("text/css"), mapCss.getBytes());

		block.append(mapHtml);
		
		return Promise.pure(block);


	};
	
	private String getBBox(JsonNode viewerstate) {
		
		String bbox = "";
		JsonNode extentNode = viewerstate.path("extent");
		if(extentNode!=null){
			bbox += extentNode.path("minx") + "," +  extentNode.path("miny") + "," +
					extentNode.path("maxx") + "," +  extentNode.path("maxy");
		}
		JsonNode scaleNode = viewerstate.path("scale");
		if(scaleNode!=null){
			//TODO
		}
		return bbox;
	}

	private String getRequestUrl (ServiceRequest request) {
		
		String serviceType = request.getService().getIdentification().getServiceType();
		String serviceEndPoint = request.getService().getIdentification().getServiceEndpoint();
		String serviceVersion = request.getService().getIdentification().getServiceVersion();
		//TODO check supported formats
		Object serviceParameters = request.getParameters();
		//TODO parse paraemters
		
		String requestTag = "";
		if (serviceType.equals("WMS")){
			String requestUrl= serviceEndPoint + "?SERVICE=" + serviceType + "&VERSION=" + serviceVersion +  "&REQUEST=GetMap&FORMAT=image%2Fsvg%2Bxml" +
					"&layers=afdelingen_test&transparent=true&CRS=EPSG%3A28992&STYLES=";
			
			
			return requestUrl;
			
		}
		
		return null;
	}

	private String getLayerCss(int layernr) {
		 return "#map_layer"+ layernr + " {" +
			    "position: absolute;" +
			    "z-index: " + layernr + ";" +
			    "left: 0;" +
			    "top: 0;" + 
			"}";
	}
	private String getObjectCss(int objectnr, int width, int height) {
		 return "#object" + objectnr + " {" +
			    "width:" + width + ";" +
		 		"height:" + height + ";" +
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
