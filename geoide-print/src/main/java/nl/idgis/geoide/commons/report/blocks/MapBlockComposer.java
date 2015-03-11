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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * A specialized composer component that composes a mapBlock with information from the client (viewerstate)
 * and a (part of) a report template
 **/ 
public class MapBlockComposer implements BlockComposer {
	final MapView mapView;
	final DocumentCache documentCache;
	private URI mapCssUri;
	
	
	/**
	 * Constructs a mapblockcomposer object.
	 * 
	 * @param mapview		the current mapview object 
	 * @param documentCache	a documentCache object to (tempory) store the css and svg files.
	 */
	
	public MapBlockComposer(MapView mapView, DocumentCache documentCache) {
		super();
		this.mapView = mapView;
		this.documentCache= documentCache;
	}

	/**
	 * Composes a mapBlock, i.e. a html snippet with related files (css and svg) stored in the documentcache, resulting
	 * in a map in a report.
	 * 
	 * @param blockInfo		client information related to this (map)block 
	 * @param block 		the html template for the map block
	 * @param reportData 	object containing some general reportdata such as width and height of a report page 
	 * @return				a promise (block object0 containing  a "filled" html snippet (map block) and a related css, 
	 */
	
	@Override
	public Promise<Block> compose(JsonNode blockInfo, Element block, ReportData reportData) throws Throwable {
		
		final JsonNode viewerstate = blockInfo.get("viewerstate");
		final double resolution = viewerstate.path("resolution").asDouble();
		final JsonNode viewerExtent = viewerstate.path("extent");
		final int viewerScale = viewerstate.path("scale").asInt();	
		
		int gridWidth;
		try {
			gridWidth = Integer.parseInt(block.attr("data-grid-width"));
		} catch (NumberFormatException e) {
			gridWidth = 12;
		}
		int gridHeight;
		try {
			gridHeight = Integer.parseInt(block.attr("data-grid-height"));
		} catch (NumberFormatException e) {
			gridHeight = 12;
		}
		final boolean scaleFixed = Boolean.getBoolean(block.attr("data-scale-fixed"));
		
		
		final double blockWidthmm = reportData.getReportWidth() * gridWidth/12; 
		final double blockHeightmm = reportData.getReportHeight() * gridHeight/12; 
		
		CenterScale centerScale = new CenterScale (scaleFixed, viewerScale, viewerExtent, blockWidthmm, blockHeightmm );
		
		
			
		//van mm naar px
		double blockWidthpx  = blockWidthmm / 0.28;
		double mapWidthm = (blockWidthmm * centerScale.getScale())/1000;
		double mapHeightm = (blockHeightmm * centerScale.getScale())/1000;
		
		double resizeFactor = resolution/(mapWidthm/blockWidthpx);
		double blockHeightpx = blockHeightmm / 0.28;
		if( blockHeightpx == 0 ){
			blockHeightpx = (blockWidthpx/mapWidthm * mapHeightm); 
		}
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode reportExtent = mapper.createObjectNode();
		reportExtent.put ("minx", centerScale.getCenterX() - mapWidthm/2);
		reportExtent.put ("maxx", centerScale.getCenterX() + mapWidthm/2);
		reportExtent.put ("miny", centerScale.getCenterY() - mapHeightm/2);
		reportExtent.put ("maxy", centerScale.getCenterY() + mapHeightm/2);
		
		
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
				List<JsonNode>  requestUrls = layerServiceType.getLayerRequestUrls(request, reportExtent, resolution,(int) blockWidthpx, (int) blockHeightpx);
				
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
		
		Block mapBlock = new Block(block, mapCssUri);
		return Promise.pure(mapBlock);


	};

	
	/**
	 * method that composes a css-snippet for a map layer 
	 * 
	 * @param layernr			a unique number for a layer (defines the z-index in the report)
	 * @param blockHeightpx	 	the height of the (map)block in the template in pixels		
	 * @param blockWidthpx 		the width of the (map)block in the template in pixels
	 * @return					a css snippet (String)
	 */
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
		

}
