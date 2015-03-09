package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.report.ReportData;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.map.MapView;
import nl.idgis.geoide.map.MapView.LayerWithState;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.ogc.util.MimeContentType;

import org.jsoup.nodes.Document;
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
			    	"overflow: hidden;" + 
				"}" +
				".pos-abs {" +
					"position: absolute;" +
				"}";
		
		int layernr = 1;
		
		
		for (final ServiceRequest request: serviceRequests) {			
			ServiceType serviceType = mapView.getServiceType(request.getService());
			
			if (serviceType instanceof LayerServiceType ) {
				
				Element mapLayer = mapRow.appendElement("div");
				mapLayer.attr("id", "map_layer" + layernr);
				mapCss += getLayerCss(layernr, blockHeightpx, blockWidthpx);
				Element layerObject = mapLayer.appendElement("object");
				layerObject.attr("type", "image/svg+xml");
				layerObject.attr("style", "left:0px; top:0px; width:" + (int) blockWidthpx + "px; height:" + (int) blockHeightpx + "px;");
				
				
				LayerServiceType layerServiceType = (LayerServiceType) serviceType;
				List<JsonNode>  requestUrls = layerServiceType.getLayerRequestUrls(request, viewerstate.path("extent"), resolution,(int) blockWidthpx, (int) blockHeightpx);
				
				if (request.getService().getIdentification().getServiceType().equals("TMS")){
					URI layerSvgUri = new URI ("stored://" + UUID.randomUUID ().toString ());
					//create svg document
					Document layerSvg = new Document(layerSvgUri.toString());
					Element svgNode = layerSvg.appendElement("svg"); 
					svgNode.attr("width", blockWidthpx + "px");
					svgNode.attr("height", blockHeightpx + "px");
					svgNode.attr("version", "1.1");
					svgNode.attr("xmlns","http://www.w3.org/2000/svg");
					svgNode.attr("xmlns:xlink", "http://www.w3.org/1999/xlink");
					for (JsonNode requestUrl:requestUrls) {
						//write to svg
						Element svgImage = svgNode.appendElement("image");
						svgImage.attr("xlink:href", requestUrl.path("uri").asText());
						svgImage.attr("x", String.valueOf( requestUrl.path("left").asDouble() * resizeFactor));
						svgImage.attr("y", String.valueOf( requestUrl.path("bottom").asDouble() * resizeFactor));
						svgImage.attr("width",  String.valueOf((requestUrl.path("right").asDouble() * resizeFactor) - (requestUrl.path("left").asDouble() * resizeFactor)));
						svgImage.attr("height",  String.valueOf(((requestUrl.path("bottom").asDouble() * resizeFactor)- requestUrl.path("top").asDouble() * resizeFactor)));
						//<image xlink:href="http://geodata.nationaalgeoregister.nl/tms/1.0.0/brtachtergrondkaart/1.0.0/9/280/219.png" x="-100" y="0px" height="150" width="150"/>	
					}
					
					documentCache.store(layerSvgUri, new MimeContentType ("image/svg+xml"), layerSvg.toString().getBytes());
					//write object tag in html
					layerObject.attr("data", layerSvgUri.toString());	
					
					System.out.println("*****" + layerSvg.toString());
					
					
				} else {
					//write directly to html
					layerObject.attr("data", requestUrls.get(0).path("uri").asText());	
				}	
				
				layernr += 1;
				
			}

			
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
