package nl.idgis.geoide.commons.report.blocks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.feature.FeatureOverlay;
import nl.idgis.geoide.commons.domain.feature.Overlay;
import nl.idgis.geoide.commons.domain.feature.OverlayFeature;
import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.report.render.OverlayRenderer;
import nl.idgis.geoide.commons.report.render.OverlayRenderer.PositionedTextOverlay;
import nl.idgis.geoide.commons.report.render.SvgRenderer.SvgPoint;
import nl.idgis.geoide.map.DefaultMapView;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.util.Futures;

/**
 * A specialized composer component that composes a mapBlock with information
 * from the client (viewerstate) and a (part of) a report template
 **/

public class MapBlockComposer implements BlockComposer<MapBlockInfo> {
	final DefaultMapView mapView;
	private URI mapCssUri;
	private static Logger log = LoggerFactory.getLogger(MapBlockComposer.class);

	/**
	 * Constructs a mapblockcomposer object.
	 * 
	 * @param mapview
	 *            the current mapview object
	 * @param documentCache
	 *            a documentCache object to (tempory) store the css and svg
	 *            files.
	 */

	public MapBlockComposer(DefaultMapView mapView) {
		this.mapView = mapView;
	}

	/**
	 * Composes a mapBlock, i.e. a html snippet with related files (css and svg)
	 * stored in the documentcache, resulting in a map in a report.
	 * 
	 * @param info
	 *            client information related to this (map)block (MapBlockInfo)
	 * @param blockElement
	 *            the html template Element for the map block
	 * @param documentCache
	 *            documentCacheobject to store the css, svgTile documents
	 * @return a promise (block object containing a "filled" html snippet (map
	 *         block) and a related css,
	 */

	@Override
	public CompletableFuture<Block> compose(Element blockElement, MapBlockInfo mapInfo, DocumentCache documentCache,
			String token) throws Throwable {
		Element mapRow = blockElement.appendElement("div");
		mapRow.attr("class", "map_row");

		return mapView.flattenLayerList(JsonFactory.externalize(mapInfo.getClientInfo()), token)
				.thenCompose((layerStates) -> mapView.getServiceRequests(layerStates).thenCompose((serviceRequests) -> {
					final List<CompletableFuture<nl.idgis.geoide.commons.domain.document.Document>> documentPromises = new ArrayList<>();

					String mapCss = createMapCss(mapInfo);

					int layernr = 1;

					int widthpx = mapInfo.getWidthpx();
					int heightpx = mapInfo.getHeightpx();
					double widthmm = mapInfo.getBlockWidth();
					double heightmm = mapInfo.getBlockHeight();

					for (final ServiceRequest request : serviceRequests) {
						
						ServiceType serviceType = mapView.getServiceType(request.getService());

						if (serviceType instanceof LayerServiceType) {
							String printFormat = request.getService().getPrintFormat();
							if (printFormat == null) {
								printFormat = "image/svg+xml";
							}
							LayerServiceType layerServiceType = (LayerServiceType) serviceType;
							
							List<JsonNode> requestUrls = layerServiceType.getLayerRequestUrls(request,
									mapInfo.getMapExtentJson(), mapInfo.getResolution(), widthpx, heightpx);

							if (request.getService().getIdentification().getServiceType().equals("TMS")) {

								for (JsonNode requestUrl : requestUrls) {
									Element mapLayer = createLayerElement(mapRow, widthmm, heightmm, layernr);
									mapCss += getLayerCss(layernr, mapInfo);
									URI tileSvgUri = createUri("stored://" + UUID.randomUUID().toString());
									Document tileSvg = createTileSvg(tileSvgUri, requestUrl, mapInfo);
									documentPromises.add(documentCache.store(tileSvgUri,
											new MimeContentType("image/svg+xml"), tileSvg.toString().getBytes()));
									mapLayer.childNode(0).attr("data", tileSvgUri.toString());
									layernr += 1;
								}

							} else {
								Element mapLayer = createLayerElement(mapRow, widthmm, heightmm, layernr);
								mapCss += getLayerCss(layernr, mapInfo);
								if (printFormat.indexOf("svg") == -1) {
									Element imgElem = ((Element) mapLayer.childNode(0)).appendElement("img");
									imgElem.attr("src", requestUrls.get(0).path("uri").asText())
											.attr("height", heightmm + "mm").attr("width", widthmm + "mm")
											.attr("style", "left:0;top:0;padding:0;margin:0;border:0");
									System.out.println("MapBlockComposer: compose: requestUrl: img " + requestUrls.get(0).path("uri").asText());
								} else {
									mapLayer.childNode(0).attr("data", requestUrls.get(0).path("uri").asText());
									System.out.println("MapBlockComposer: compose: requestUrl: svg " + requestUrls.get(0).path("uri").asText());
								}
								
								
								layernr += 1;

							}

						}

					}

					// Add overlay features:
					final URI overlaySvgUri = createUri("stored://" + UUID.randomUUID().toString());
					documentPromises.add(documentCache.store(overlaySvgUri, new MimeContentType("image/svg+xml"),
							createOverlaySvg(mapInfo, mapInfo.getOverlays())));
					createOverlayElement(mapRow, widthmm, heightmm, ++layernr, overlaySvgUri.toString());
					mapCss += getOverlayCss(mapInfo);

					// Add text overlays:
					for (final FeatureOverlay overlay : mapInfo.getOverlays()) {
						for (final OverlayFeature overlayFeature : overlay.getFeatures()) {
							if (overlayFeature.getOverlay() == null) {
								continue;
							}

							final URI textOverlaySvgUri = createUri("stored://" + UUID.randomUUID().toString());
							final TextOverlayRenderResult renderResult = createTextOverlaySvg(mapInfo, overlayFeature);
							documentPromises.add(documentCache.store(textOverlaySvgUri,
									new MimeContentType("image/svg+xml"), renderResult.bytes));

							createOverlayElement(mapRow, widthmm, heightmm, ++layernr, textOverlaySvgUri.toString());
							createOverlayBoxElement(mapRow, mapInfo, widthmm, heightmm, ++layernr,
									renderResult.positionedOverlay);
						}
					}

					mapCssUri = createUri("stored://" + UUID.randomUUID().toString());
					documentPromises
							.add(documentCache.store(mapCssUri, new MimeContentType("text/css"), mapCss.getBytes()));
					log.debug("mapblock css:" + mapCss);

					final Block mapBlock = new Block(blockElement, mapCssUri);

					return Futures.all(documentPromises).thenApply(
							(final List<nl.idgis.geoide.commons.domain.document.Document> documents) -> mapBlock);
				}));
	}

	private static URI createUri(final String content) {
		try {
			return new URI(content);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private Element createOverlayElement(final Element mapRow, final double width, final double height,
			final int zIndex, final String uri) {
		final Element overlayElement = mapRow.appendElement("div").addClass("map-overlays").attr("style",
				"z-index:" + zIndex + ";");

		overlayElement.appendElement("object").attr("type", "image/svg+xml")
				.attr("style", "left:0;top:0;width:" + width + "mm;height:" + height + "mm;").attr("data", uri);

		return overlayElement;
	}

	private void createOverlayBoxElement(final Element mapRow, final MapBlockInfo info, final double width,
			final double height, final int zIndex, final PositionedTextOverlay positionedOverlay) {
		if (positionedOverlay == null) {
			return;
		}

		final Overlay overlay = positionedOverlay.getOverlay();
		if (overlay == null) {
			return;
		}

		final SvgPoint point = positionedOverlay.getPosition();

		final Envelope extent = info.getMapExtent();
		final double pixelWidth = (extent.getMaxX() - extent.getMinX()) / info.getResolution();
		final double pixelHeight = Math.abs(extent.getMaxY() - extent.getMinY()) / info.getResolution();
		final double anchorX = ((point.getX() - extent.getMinX()) / (extent.getMaxX() - extent.getMinX())) * pixelWidth;
		final double anchorY = ((point.getY() - extent.getMinY()) / (extent.getMaxY() - extent.getMinY()))
				* pixelHeight;
		final double rx = width / pixelWidth;
		final double ry = height / pixelHeight;

		final Element boxElement = mapRow.appendElement("div").addClass("map-overlay-text-box").attr("style",
				"display: block; position: absolute; z-index: " + zIndex + "; "
						+ String.format(Locale.US, "left:%fmm;", (anchorX + 2) * rx)
						+ String.format(Locale.US, "top:%fmm;", (anchorY + 2) * ry)
						+ String.format(Locale.US, "width:%fmm;", (overlay.getWidth() - 4) * rx)
						+ String.format(Locale.US, "height:%fmm;", (overlay.getHeight() - 4) * ry));

		if (overlay.getText() != null) {
			boxElement.appendText(overlay.getText());
		}
	}

	private Element createLayerElement(Element mapRow, double width, double height, int layernr) {
		Element mapLayer = mapRow.appendElement("div");
		mapLayer.attr("id", "map_layer" + layernr);
		Element layerObject = mapLayer.appendElement("object");
		layerObject.attr("type", "image/svg+xml").attr("style",
				"left:0;top:0;width:" + width + "mm; height:" + height + "mm;");
		return mapLayer;
	}

	/**
	 * method that create a svg Document with one tile
	 * 
	 * @param tileSvgUri
	 *            URI for the Document
	 * @param requestUrl
	 *            tile request Url
	 * @param mapBlockInfo
	 *            Information about the mapBlock
	 * @return a tileSvg Document
	 */

	private Document createTileSvg(URI tileSvgUri, JsonNode requestUrl, MapBlockInfo info) {
		Document tileSvg = new Document(tileSvgUri.toString());
		Element svgNode = tileSvg.appendElement("svg");
		double factor = info.getResizeFactor(requestUrl.path("resolution").asDouble());

		svgNode // .attr ("viewBox", "0 0 " + info.getWidthpx() + " " +
				// info.getHeightpx() + "")
				.attr("width", info.getWidthpx() + "px").attr("height", info.getHeightpx() + "px")
				.attr("version", "1.1").attr("xmlns", "http://www.w3.org/2000/svg")
				.attr("xmlns:xlink", "http://www.w3.org/1999/xlink");

		Element svgImage = svgNode.appendElement("image");
		svgImage.attr("xlink:href", requestUrl.path("uri").asText());
		svgImage.attr("x", String.valueOf(requestUrl.path("left").asDouble() * factor));
		svgImage.attr("y", String.valueOf(requestUrl.path("top").asDouble() * factor));
		svgImage.attr("width",
				String.valueOf((requestUrl.path("right").asDouble() - requestUrl.path("left").asDouble()) * factor));
		svgImage.attr("height",
				String.valueOf((requestUrl.path("bottom").asDouble() - requestUrl.path("top").asDouble()) * factor));

		return tileSvg;

	}

	/**
	 * method that create css for a report map
	 * 
	 * @param centerScale
	 *            the mapBlockCenterScale object containing map information
	 * @return a css snippet for a report map (String)
	 */
	private String createMapCss(MapBlockInfo blockInfo) {
		String mapCss = ".map_row {" + "border-color:green; border-size:1px;" + "height: " + blockInfo.getBlockHeight()
				+ "mm;" + "width: " + blockInfo.getBlockWidth() + "mm;" + "position: relative;" + "overflow: hidden;"
				+ "}" + ".pos-abs {" + "position: absolute;" + "}";
		return mapCss;
	}

	/**
	 * method that composes a css-snippet for a map layer
	 * 
	 * @param layernr
	 *            a unique number for a layer (defines the z-index in the
	 *            report)
	 * @param centerScale
	 *            the mapBlockCenterScale object containing map information
	 * @return a css snippet for a mapLayer (String)
	 */

	private String getLayerCss(int layernr, MapBlockInfo blockInfo) {
		return "#map_layer" + layernr + " {" + "position: absolute;" + "z-index: " + layernr + ";" + "left: 0;"
				+ "top: 0;" + "height: " + blockInfo.getBlockHeight() + "mm;" + "width: " + blockInfo.getBlockWidth()
				+ "mm;" + "}";
	}

	private String getOverlayCss(final MapBlockInfo blockInfo) {
		return ".map-overlays { position: absolute; left: 0; top: 0; width:" + blockInfo.getBlockWidth() + "mm; height:"
				+ blockInfo.getBlockHeight() + "mm;}";
	}

	protected byte[] createOverlaySvg(final MapBlockInfo info, final List<FeatureOverlay> overlays) {
		try {
			final OverlayRenderer renderer = new OverlayRenderer(info.getMapExtent(), info.getResolution());

			final ByteArrayOutputStream os = new ByteArrayOutputStream();

			final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);

			renderer.overlays(writer, overlays);

			writer.close();
			os.close();

			return os.toByteArray();
		} catch (XMLStreamException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected TextOverlayRenderResult createTextOverlaySvg(final MapBlockInfo info, final OverlayFeature feature) {
		try {
			final OverlayRenderer renderer = new OverlayRenderer(info.getMapExtent(), info.getResolution());

			final ByteArrayOutputStream os = new ByteArrayOutputStream();

			final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);

			final PositionedTextOverlay positionedOverlay = renderer.textOverlay(writer, feature);

			writer.close();
			os.close();

			return new TextOverlayRenderResult(positionedOverlay, os.toByteArray());
		} catch (XMLStreamException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected static class TextOverlayRenderResult {
		public final PositionedTextOverlay positionedOverlay;
		public final byte[] bytes;

		public TextOverlayRenderResult(final PositionedTextOverlay positionedOverlay, final byte[] bytes) {
			this.positionedOverlay = positionedOverlay;
			this.bytes = bytes;
		}
	}
}
