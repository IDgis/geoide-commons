package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.map.MapView;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.ogc.util.MimeContentType;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import play.libs.F.Function;
import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * A specialized composer component that composes a mapBlock with information from the client (viewerstate)
 * and a (part of) a report template
 **/ 


public class MapBlockComposer implements BlockComposer {
	final MapView mapView;
	private URI mapCssUri;
	
	
	/**
	 * Constructs a mapblockcomposer object.
	 * 
	 * @param mapview		the current mapview object 
	 * @param documentCache	a documentCache object to (tempory) store the css and svg files.
	 */
	
	public MapBlockComposer(MapView mapView) {
		this.mapView = mapView;
	}

		
	/**
	 * Composes a mapBlock, i.e. a html snippet with related files (css and svg) stored in the documentcache, resulting
	 * in a map in a report.
	 * 
	 * @param info			client information related to this (map)block (MapBlockInfo)
	 * @param blockElement 	the html template Element for the map block
	 * @param documentCache documentCacheobject to store the css, svgTile documents 
	 * @return				a promise (block object containing  a "filled" html snippet (map block) and a related css, 
	 */
	
	@Override
	public Promise<Block> compose (Element blockElement, BlockInfo info, DocumentCache documentCache) throws Throwable {
		
		MapBlockInfo mapInfo = (MapBlockInfo) info;
		final List<Promise<nl.idgis.geoide.documentcache.Document>> documentPromises = new ArrayList<> ();
		
		String mapCss = createMapCss(mapInfo);
		
		Element mapRow = blockElement.appendElement("div");
		mapRow.attr("class", "map_row");
				
		final List<ServiceRequest> serviceRequests = mapView.getServiceRequests (mapView.flattenLayerList (info.getClientInfo()));	
		int layernr = 1;
		
		int width = mapInfo.getWidthpx();
		int height = mapInfo.getHeightpx();
		
		

		for (final ServiceRequest request: serviceRequests) {				
			ServiceType serviceType = mapView.getServiceType(request.getService());
			
			if (serviceType instanceof LayerServiceType ) {
				LayerServiceType layerServiceType = (LayerServiceType) serviceType;
				List<JsonNode>  requestUrls = layerServiceType.getLayerRequestUrls(request, mapInfo.getMapExtent(), mapInfo.getResolution(), width, height);
			
				if (request.getService().getIdentification().getServiceType().equals("TMS")){
					
					for (JsonNode requestUrl:requestUrls) {
						Element mapLayer = createLayerElement(mapRow, width, height, layernr);
						mapCss += getLayerCss (layernr, mapInfo);
						URI tileSvgUri = new URI ("stored://" + UUID.randomUUID ().toString ());
						Document tileSvg = createTileSvg (tileSvgUri, requestUrl, mapInfo);
						documentPromises.add (documentCache.store(tileSvgUri, new MimeContentType ("image/svg+xml"), tileSvg.toString().getBytes()));
						mapLayer.childNode(0).attr("data", tileSvgUri.toString());
						layernr += 1;
					}	
					
				} else {
					
					Element mapLayer = createLayerElement(mapRow, width, height, layernr);					
					mapCss += getLayerCss (layernr, mapInfo);
					mapLayer.childNode(0).attr("data", requestUrls.get(0).path("uri").asText());	
					layernr += 1;
				
				}	
				
			}

		}
				
		mapCssUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		documentPromises.add (documentCache.store(mapCssUri, new MimeContentType ("text/css"), mapCss.getBytes()));
		
		final Block mapBlock = new Block(blockElement, mapCssUri);
		
		return Promise
			.sequence (documentPromises)
			.map (new Function<List<nl.idgis.geoide.documentcache.Document>, Block> () {
				@Override
				public Block apply (
						final List<nl.idgis.geoide.documentcache.Document> documents)
						throws Throwable {
					return mapBlock;
				}
			});
	};

	
	private Element createLayerElement(Element mapRow, int width, int height, int layernr) {
		Element mapLayer = mapRow.appendElement("div");
		mapLayer.attr("id", "map_layer" + layernr);		
		Element layerObject = mapLayer.appendElement("object");
		layerObject	.attr("type", "image/svg+xml")
					.attr("style", "left:0;px;top:0px; width:" + width + "px; height:" + height + "px;");
		return mapLayer;
	}


	/**
	 * method that create a svg Document with one tile  
	 * @param tileSvgUri 	URI for the Document
	 * @param requestUrl	tile request Url	
	 * @param mapBlockInfo	Information about the mapBlock
	 * @return				a tileSvg Document
	 */
	
	private Document createTileSvg (URI tileSvgUri, JsonNode requestUrl, MapBlockInfo info) {	
		Document tileSvg = new Document(tileSvgUri.toString());
		Element svgNode = tileSvg.appendElement("svg"); 
		double factor = info.getResizeFactor();
		
		svgNode	.attr ("viewBox", "0 0 " + info.getWidthpx() + " " + info.getHeightpx() + "")
				.attr ("width", info.getWidthpx() + "px")
				.attr ("height", info.getHeightpx() + "px")
				.attr ("version", "1.1")
				.attr ("xmlns","http://www.w3.org/2000/svg")
				.attr ("xmlns:xlink", "http://www.w3.org/1999/xlink");

		Element svgImage = svgNode.appendElement("image");
		svgImage.attr("xlink:href", requestUrl.path("uri").asText());
		svgImage.attr("x", String.valueOf( requestUrl.path("left").asDouble() * factor));
		svgImage.attr("y", String.valueOf( requestUrl.path("top").asDouble() * factor));
		svgImage.attr("width",  String.valueOf((requestUrl.path("right").asDouble()* factor) - (requestUrl.path("left").asDouble()) * factor));
		svgImage.attr("height",  String.valueOf((requestUrl.path("bottom").asDouble() * factor)- (requestUrl.path("top").asDouble()) * factor));

		return tileSvg;
		
	}

	/**
	 * method that create css for a report map  
	 * @param centerScale  		the mapBlockCenterScale object containing map information		
	 * @return					a css snippet for a report map (String)
	 */
	private String createMapCss(MapBlockInfo blockInfo) {
		String mapCss = ".map_row {" +
			    	"height: " + blockInfo.getHeightpx() + "px;" + 
			    	"width: " + blockInfo.getWidthpx() + "px;" +
			    	"position: relative;" +
			    	"border: 1px solid gray;" + 
			    	"overflow: hidden;" + 
				"}" +
				".pos-abs {" +
					"position: absolute;" +
				"}";
		return mapCss;
	}
	
	
	/**
	 * method that composes a css-snippet for a map layer 
	 * @param layernr			a unique number for a layer (defines the z-index in the report)
	 * @param centerScale  		the mapBlockCenterScale object containing map information		
	 * @return					a css snippet for a mapLayer (String)
	 */
	
	private String getLayerCss(int layernr, MapBlockInfo blockInfo) {
		 return "#map_layer"+ layernr + " {" +
			    "position: absolute;" +
			    "z-index: " + layernr + ";" +
			    "left: 0px;" +
			    "top: 0px;" + 
			    "height: " + blockInfo.getHeightpx() + "px;" + 
				"width: " + blockInfo.getWidthpx() + "px;" +
			"}";
	}
		

}
