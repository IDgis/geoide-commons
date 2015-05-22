package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.feature.FeatureOverlay;
import nl.idgis.geoide.commons.domain.feature.Overlay;
import nl.idgis.geoide.commons.domain.feature.OverlayFeature;
import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.print.service.HtmlPrintService;
import nl.idgis.geoide.commons.report.render.OverlayRenderer;
import nl.idgis.geoide.commons.report.render.OverlayRenderer.PositionedTextOverlay;
import nl.idgis.geoide.commons.report.render.SvgRenderer.SvgPoint;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.map.MapView;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.ogc.util.MimeContentType;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.F.Function;
import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * A specialized composer component that composes a mapBlock with information from the client (viewerstate)
 * and a (part of) a report template
 **/ 


public class MapBlockComposer implements BlockComposer<MapBlockInfo> {
	final MapView mapView;
	private URI mapCssUri;
	private static Logger log = LoggerFactory.getLogger (HtmlPrintService.class);
	
	
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
	public Promise<Block> compose (Element blockElement, MapBlockInfo mapInfo, DocumentCache documentCache) throws Throwable {
		
		final List<Promise<nl.idgis.geoide.documentcache.Document>> documentPromises = new ArrayList<> ();
		
		String mapCss = createMapCss(mapInfo);
		
		Element mapRow = blockElement.appendElement("div");
		mapRow.attr("class", "map_row");
				
		final List<ServiceRequest> serviceRequests = mapView.getServiceRequests (mapView.flattenLayerList (mapInfo.getClientInfo()));	
		int layernr = 1;
		
		int widthpx = mapInfo.getWidthpx();
		int heightpx = mapInfo.getHeightpx();
		double widthmm = mapInfo.getBlockWidth();
		double heightmm = mapInfo.getBlockHeight();
		

		for (final ServiceRequest request: serviceRequests) {				
			ServiceType serviceType = mapView.getServiceType(request.getService());
			
			if (serviceType instanceof LayerServiceType ) {
				LayerServiceType layerServiceType = (LayerServiceType) serviceType;
				List<JsonNode>  requestUrls = layerServiceType.getLayerRequestUrls(request, mapInfo.getMapExtentJson(), mapInfo.getResolution(), widthpx, heightpx);
			
				if (request.getService().getIdentification().getServiceType().equals("TMS")){
					
					for (JsonNode requestUrl:requestUrls) {
						Element mapLayer = createLayerElement(mapRow, widthmm, heightmm, layernr);
						mapCss += getLayerCss (layernr, mapInfo);
						URI tileSvgUri = new URI ("stored://" + UUID.randomUUID ().toString ());
						Document tileSvg = createTileSvg (tileSvgUri, requestUrl, mapInfo);
						documentPromises.add (documentCache.store(tileSvgUri, new MimeContentType ("image/svg+xml"), tileSvg.toString().getBytes()));
						mapLayer.childNode(0).attr("data", tileSvgUri.toString());
						layernr += 1;
					}	
					
				} else {
					
					Element mapLayer = createLayerElement(mapRow, widthmm, heightmm, layernr);					
					mapCss += getLayerCss (layernr, mapInfo);
					mapLayer.childNode(0).attr("data", requestUrls.get(0).path("uri").asText());	
					layernr += 1;
				
				}	
				
			}

		}
		
		// Add overlay features:
		final URI overlaySvgUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		documentPromises.add (documentCache.store (
				overlaySvgUri, 
				new MimeContentType ("image/svg+xml"), 
				createOverlaySvg (mapInfo, mapInfo.getOverlays ())
			));
		createOverlayElement (mapRow, widthmm, heightmm, ++ layernr, overlaySvgUri.toString ());
		mapCss += getOverlayCss (mapInfo);
		
		// Add text overlays:
		for (final FeatureOverlay overlay: mapInfo.getOverlays ()) {
			for (final OverlayFeature overlayFeature: overlay.getFeatures ()) {
				if (overlayFeature.getOverlay () == null) {
					continue;
				}
				
				final URI textOverlaySvgUri = new URI ("stored://" + UUID.randomUUID ().toString ());
				final TextOverlayRenderResult renderResult = createTextOverlaySvg (mapInfo, overlayFeature);
				documentPromises.add (documentCache.store (
					textOverlaySvgUri,
					new MimeContentType ("image/svg+xml"),
					renderResult.bytes
				));
				
				createOverlayElement (mapRow, widthmm, heightmm, ++ layernr, textOverlaySvgUri.toString ());
				createOverlayBoxElement (mapRow, mapInfo, widthmm, heightmm, ++ layernr, renderResult.positionedOverlay);
			}
		}
				
		mapCssUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		documentPromises.add (documentCache.store(mapCssUri, new MimeContentType ("text/css"), mapCss.getBytes()));
		log.debug("mapblock css:" + mapCss);
		
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

	private Element createOverlayElement (final Element mapRow, final double width, final double height, final int zIndex, final String uri) {
		final Element overlayElement = mapRow
			.appendElement ("div")
			.addClass ("map-overlays")
			.attr ("style", "z-index:" + zIndex + ";");
		
		overlayElement
			.appendElement ("object")
			.attr ("type", "image/svg+xml")
			.attr ("style", "left:0;top:0;width:" + width + "mm;height:" + height + "mm;")
			.attr ("data", uri);
		
		return overlayElement;
	}
	
	private void createOverlayBoxElement (final Element mapRow, final MapBlockInfo info, final double width, final double height, final int zIndex, final PositionedTextOverlay positionedOverlay) {
		if (positionedOverlay == null) {
			return;
		}
		
		final Overlay overlay = positionedOverlay.getOverlay ();
		if (overlay == null) {
			return;
		}
		
		final SvgPoint point = positionedOverlay.getPosition ();
		
		final Envelope extent = info.getMapExtent ();
		final double pixelWidth = (extent.getMaxX () - extent.getMinX ()) / info.getResolution ();
		final double pixelHeight = Math.abs (extent.getMaxY() - extent.getMinY ()) / info.getResolution ();
		final double anchorX = ((point.getX () - extent.getMinX ()) / (extent.getMaxX () - extent.getMinX ())) * pixelWidth;
		final double anchorY = ((point.getY () - extent.getMinY ()) / (extent.getMaxY () - extent.getMinY ())) * pixelHeight;
		final double rx = width / pixelWidth;
		final double ry = height / pixelHeight;
		
		final Element boxElement = mapRow
				.appendElement ("div")
				.addClass ("map-overlay-text-box")
				.attr (
					"style", "display: block; position: absolute; z-index: " + zIndex + "; "
					+ String.format (Locale.US, "left:%fmm;", (anchorX + 2) * rx)
					+ String.format (Locale.US, "top:%fmm;", (anchorY + 2) * ry)
					+ String.format (Locale.US, "width:%fmm;", (overlay.getWidth () - 4) * rx)
					+ String.format (Locale.US, "height:%fmm;", (overlay.getHeight () - 4) * ry)
				);
				

		if (overlay.getText () != null) {
			boxElement.appendText (overlay.getText ());
		}
	}
	
	private Element createLayerElement(Element mapRow, double width, double height, int layernr) {
		Element mapLayer = mapRow.appendElement("div");
		mapLayer.attr("id", "map_layer" + layernr);		
		Element layerObject = mapLayer.appendElement("object");
		layerObject	.attr("type", "image/svg+xml")
					.attr("style", "left:0;top:0;width:" + width + "mm; height:" + height + "mm;");
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
		double factor = info.getResizeFactor(requestUrl.path("resolution").asDouble());
		
		svgNode	//.attr ("viewBox", "0 0 " + info.getWidthpx() + " " + info.getHeightpx() + "")
				.attr ("width", info.getWidthpx() + "px" )
				.attr ("height", info.getHeightpx()  + "px")
				.attr ("version", "1.1")
				.attr ("xmlns","http://www.w3.org/2000/svg")
				.attr ("xmlns:xlink", "http://www.w3.org/1999/xlink");

		Element svgImage = svgNode.appendElement("image");
		svgImage.attr("xlink:href", requestUrl.path("uri").asText());
		svgImage.attr("x", String.valueOf( requestUrl.path("left").asDouble() * factor));
		svgImage.attr("y", String.valueOf( requestUrl.path("top").asDouble() * factor));
		svgImage.attr("width",  String.valueOf((requestUrl.path("right").asDouble() - requestUrl.path("left").asDouble()) * factor));
		svgImage.attr("height",  String.valueOf((requestUrl.path("bottom").asDouble()- requestUrl.path("top").asDouble()) * factor));

		return tileSvg;
		
	}

	/**
	 * method that create css for a report map  
	 * @param centerScale  		the mapBlockCenterScale object containing map information		
	 * @return					a css snippet for a report map (String)
	 */
	private String createMapCss(MapBlockInfo blockInfo) {
		String mapCss = ".map_row {" +
					"border-color:green; border-size:1px;" + 
			    	"height: " + blockInfo.getBlockHeight() + "mm;" + 
			    	"width: " + blockInfo.getBlockWidth() + "mm;" +
			    	"position: relative;" +
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
			    "left: 0;" +
			    "top: 0;" + 
			    "height: " + blockInfo.getBlockHeight() + "mm;" + 
				"width: "+ blockInfo.getBlockWidth() + "mm;" +
			"}";
	}
	
	private String getOverlayCss (final MapBlockInfo blockInfo) {
		return ".map-overlays { position: absolute; left: 0; top: 0; width:" 
				+ blockInfo.getBlockWidth () + "mm; height:" + blockInfo.getBlockHeight () + "mm;}"; 
	}
		
	protected byte[] createOverlaySvg (final MapBlockInfo info, final List<FeatureOverlay> overlays) throws Throwable {
		final OverlayRenderer renderer = new OverlayRenderer (
				info.getMapExtent (),
				info.getResolution ()
			);
		
		final ByteArrayOutputStream os = new ByteArrayOutputStream ();
		
		final XMLStreamWriter writer = XMLOutputFactory.newInstance ().createXMLStreamWriter (os);
		
		renderer.overlays (writer, overlays);
		
		writer.close();
		os.close ();
		
		return os.toByteArray ();
	}
	
	protected TextOverlayRenderResult createTextOverlaySvg (final MapBlockInfo info, final OverlayFeature feature) throws Throwable {
		final OverlayRenderer renderer = new OverlayRenderer (info.getMapExtent (), info.getResolution ());
		
		final ByteArrayOutputStream os = new ByteArrayOutputStream ();
		
		final XMLStreamWriter writer = XMLOutputFactory.newInstance ().createXMLStreamWriter (os);
		
		final PositionedTextOverlay positionedOverlay = renderer.textOverlay (writer, feature);
		
		writer.close();
		os.close ();
		
		return new TextOverlayRenderResult (positionedOverlay, os.toByteArray ());
	}
	
	protected static class TextOverlayRenderResult {
		public final PositionedTextOverlay positionedOverlay;
		public final byte[] bytes;
		
		public TextOverlayRenderResult (final PositionedTextOverlay positionedOverlay, final byte[] bytes) {
			this.positionedOverlay = positionedOverlay;
			this.bytes = bytes;
		}
	}
}
