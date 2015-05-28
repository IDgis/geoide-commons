package nl.idgis.geoide.commons.report.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.idgis.geoide.commons.domain.feature.FeatureOverlay;
import nl.idgis.geoide.commons.domain.feature.Overlay;
import nl.idgis.geoide.commons.domain.feature.OverlayFeature;
import nl.idgis.geoide.commons.domain.feature.StyledGeometry;
import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryCollection;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;
import nl.idgis.geoide.commons.domain.geometry.LineString;
import nl.idgis.geoide.commons.domain.geometry.Point;
import nl.idgis.geoide.commons.domain.geometry.Polygon;
import nl.idgis.geoide.commons.domain.style.FillStyle;
import nl.idgis.geoide.commons.domain.style.ImageStyle;
import nl.idgis.geoide.commons.domain.style.StrokeStyle;
import nl.idgis.geoide.commons.domain.style.Style;

public class OverlayRenderer extends SvgRenderer {

	private final Envelope envelope;
	private final double resolution;
	
	public OverlayRenderer (final Envelope envelope, final double resolution) {
		this.envelope = envelope;
		this.resolution = resolution;
	}
	
	public void overlays (final XMLStreamWriter writer, final List<FeatureOverlay> overlays) throws XMLStreamException {
		svg (writer, envelope.getMinX (), envelope.getMinY (), envelope.getMaxX () - envelope.getMinX (), envelope.getMaxY () - envelope.getMinY (), (w) -> {
			for (final FeatureOverlay o: overlays) {
				overlay (w, o);
			}
		});
	}
	
	public void overlay (final XMLStreamWriter writer, final FeatureOverlay overlay) throws XMLStreamException {
		features (writer, overlay.getFeatures ());
	}
	
	public void features (final XMLStreamWriter writer, final List<OverlayFeature> features) throws XMLStreamException {
		if (features == null) {
			return;
		}
		
		for (final OverlayFeature f: features) {
			feature (writer, f);
		}
	}
	
	public void feature (final XMLStreamWriter writer, final OverlayFeature feature) throws XMLStreamException {
		if (feature == null) {
			return;
		}
		
		// Render the geometry for this feature:
		styledGeometries (writer, feature.getStyledGeometry ());
	}

	public PositionedTextOverlay textOverlay (final XMLStreamWriter writer, final OverlayFeature feature) throws XMLStreamException {
		return svg2 (writer, envelope.getMinX (), envelope.getMinY (), envelope.getMaxX () - envelope.getMinX (), envelope.getMaxY () - envelope.getMinY (), (RenderFunction<PositionedTextOverlay>) (w) -> {
			if (feature == null) {
				return (PositionedTextOverlay) null;
			}
			
			// Render a text overlay if available:
			final Overlay overlay = feature.getOverlay ();
			if (overlay != null) {
				// Locate the styles for the overlay:
				FillStyle fillStyle = null;
				StrokeStyle strokeStyle = null;
				Point anchorPoint = null;
				for (final StyledGeometry styledGeometry: feature.getStyledGeometry ()) {
					final Style style = styledGeometry.getStyle ();
					final ImageStyle imageStyle = style.getImage ();
					
					if (imageStyle != null && imageStyle.getFill () != null) {
						fillStyle = imageStyle.getFill ();
					} else if (style.getFill () != null) {
						fillStyle = style.getFill ();
					}
					
					if (imageStyle != null && imageStyle.getStroke () != null) {
						strokeStyle = imageStyle.getStroke ();
					} else if (style.getStroke () != null){
						strokeStyle = style.getStroke (); 
					}
					
					if (styledGeometry.getGeometry ().is (GeometryType.POINT)) {
						anchorPoint = styledGeometry.getGeometry ().as (GeometryType.POINT);
					}
				}
				
				return textOverlay (writer, overlay, anchorPoint, fillStyle, strokeStyle);
			}
			
			return null;
		});
	}
	
	public PositionedTextOverlay textOverlay (final XMLStreamWriter writer, final Overlay overlay, final Point anchorPoint, final FillStyle fillStyle, final StrokeStyle strokeStyle) throws XMLStreamException {
		// Don't render the overlay if it has no visible style:
		if (overlay == null || anchorPoint == null || (fillStyle == null && strokeStyle == null)) {
			return null;
		}

		final double ox = anchorPoint.getX ();
		final double oy = anchorPoint.getY ();
		
		if (ox < envelope.getMinX () || ox > envelope.getMaxX ()) {
			return null;
		}
		if (oy < envelope.getMinY () || oy > envelope.getMaxY ()) {
			return null;
		}
		
		double minX = anchorPoint.getX () + (overlay.getOffset ().get (0) * resolution);
		double minY = anchorPoint.getY () - (overlay.getOffset ().get (1) * resolution);
		double maxX = minX + overlay.getWidth () * resolution;
		double maxY = minY - overlay.getHeight () * resolution;

		// Position the box inside the viewport:
		if (minX < envelope.getMinX ()) {
			maxX += envelope.getMinX () - minX;
			minX = envelope.getMinX ();
		}
		if (maxX > envelope.getMaxX ()) {
			minX -= maxX - envelope.getMaxX ();
			maxX = envelope.getMaxX ();
		}
		if (maxY < envelope.getMinY ()) {
			minY += envelope.getMinY () - maxY;
			maxY = envelope.getMinY ();
		}
		if (minY > envelope.getMaxY ()) {
			maxY -= minY - envelope.getMaxY ();
			minY = envelope.getMaxY ();
		}
		
		// Render the arrow:
		final double centerX = (minX + maxX) / 2;
		final double centerY = (minY + maxY) / 2;
		final double vx = centerX - anchorPoint.getX ();
		final double vy = centerY - anchorPoint.getY ();
		final double length = Math.sqrt (vx * vx + vy * vy);
		final double dx = Math.abs (length) < .0001 ? 0 : vx / length;
		final double dy = Math.abs (length) < .0001 ? 0 : vy / length;
		final double cx = dy;
		final double cy = -dx;

		final double arrowDistance = overlay.getArrowDistance () * resolution;
		final double arrowLength = overlay.getArrowLength () * resolution;
		final double arrowWidth = overlay.getArrowWidth () * resolution;
		
		path (
			writer, 
			createStroke (strokeStyle), 
			createFillFromStroke (createStroke (strokeStyle), createFill (fillStyle)), 
			Arrays.asList (new SvgPoint[] { 
				createSvgPoint (ox + dx * arrowDistance, oy + dy * arrowDistance),
				createSvgPoint (ox + dx * (arrowLength + arrowDistance) + cx * (arrowWidth / 2), oy + dy * (arrowLength + arrowDistance) + cy * (arrowWidth / 2)),
				createSvgPoint (ox + dx * (arrowLength + arrowDistance) - cx * (arrowWidth / 2), oy + dy * (arrowLength + arrowDistance) - cy * (arrowWidth / 2)),
				createSvgPoint (ox + dx * arrowDistance, oy + dy * arrowDistance)
			}), Arrays.asList (new SvgPoint[] {
				createSvgPoint (ox + dx * (arrowLength + arrowDistance), oy + dy * (arrowLength + arrowDistance)),
				createSvgPoint (centerX, centerY)
			})
		);		
		
		// Render the box:
		final SvgPoint[] points = new SvgPoint[] {
			createSvgPoint (minX, minY),
			createSvgPoint (maxX, minY),
			createSvgPoint (maxX, maxY),
			createSvgPoint (minX, maxY)
		};
		
		super.polygon (
			writer, 
			Arrays.asList (points), 
			createStroke (strokeStyle), 
			makeSolid (createFill (fillStyle))
		);
		
		return new PositionedTextOverlay (
				overlay, 
				createSvgPoint (ox, oy), 
				createSvgPoint (minX, Math.max(maxY, minY)), 
				new SvgPoint (Math.abs (maxX - minX), Math.abs (maxY - minY))
			);
	}
	
	public void styledGeometries (final XMLStreamWriter writer, final List<StyledGeometry> geometries) throws XMLStreamException {
		if (geometries == null) {
			return;
		}
		
		for (final StyledGeometry g: geometries) {
			styledGeometry (writer, g);
		}
	}
	
	public void styledGeometry (final XMLStreamWriter writer, final StyledGeometry styledGeometry) throws XMLStreamException {
		if (styledGeometry == null) {
			return;
		}
		
		final Geometry geometry = styledGeometry.getGeometry ();
		final Style style = styledGeometry.getStyle ();
		
		geometry (writer, style, geometry);
	}
	
	public void geometry (final XMLStreamWriter writer, final Style style, final Geometry geometry) throws XMLStreamException {
		if (geometry.is (GeometryType.MULTI_LINE_STRING)) {
			geometryCollection (writer, style, geometry.as (GeometryType.MULTI_LINE_STRING));
		} else if (geometry.is (GeometryType.MULTI_POINT)) {
			geometryCollection (writer, style, geometry.as (GeometryType.MULTI_POINT));
		} else if (geometry.is (GeometryType.MULTI_POLYGON)) {
			geometryCollection (writer, style, geometry.as (GeometryType.MULTI_POLYGON));
		} else if (geometry.is (GeometryType.GEOMETRY_COLLECTION)) {
			geometryCollection (writer, style, geometry.as (GeometryType.GEOMETRY_COLLECTION));
		} else if (geometry.is (GeometryType.LINE_STRING)) {
			lineString (writer, style, geometry.as (GeometryType.LINE_STRING));
		} else if (geometry.is (GeometryType.POINT)) {
			point (writer, style, geometry.as (GeometryType.POINT));
		} else if (geometry.is (GeometryType.POLYGON)) {
			polygon (writer, style, geometry.as (GeometryType.POLYGON));
		}
	}
	
	public void geometryCollection (final XMLStreamWriter writer, final Style style, final GeometryCollection<?> collection) throws XMLStreamException {
		final int n = collection.getNumGeometries ();
		for (int i = 0; i < n; ++ i) {
			geometry (writer, style, collection.getGeometryN (i));
		}
	}
	
	public void point (final XMLStreamWriter writer, final Style style, final Point point) throws XMLStreamException {
		final ImageStyle imageStyle = style.getImage ();
		if (imageStyle == null) {
			return;
		}
		
		final FillStyle fillStyle = imageStyle.getFill ();
		final StrokeStyle strokeStyle = imageStyle.getStroke ();
		final Double radius = imageStyle.getRadius ();

		// Do nothing if the point has no style or no radius:
		if ((fillStyle == null && strokeStyle == null) || radius == null || radius <= 0) {
			return;
		}
		
		final SvgPoint p = createSvgPoint (point);
		
		circle (
				writer, 
				p.getX (), 
				p.getY (), 
				radius * resolution, 
				createStroke (strokeStyle),
				createFill (fillStyle)
			);
	}
	
	public void lineString (final XMLStreamWriter writer, final Style style, final LineString lineString) throws XMLStreamException {
		final StrokeStyle strokeStyle = style.getStroke ();
		if (strokeStyle == null) {
			return;
		}
		
		final List<SvgPoint> points = new ArrayList<> (lineString.getNumPoints ());
		final int n = lineString.getNumPoints ();
		
		for (int i = 0; i < n; ++ i) {
			points.add (createSvgPoint (lineString.getPointN (i)));
		}
		
		polyline (writer, points, createStroke (strokeStyle));
	}
	
	public void polygon (final XMLStreamWriter writer, final Style style, final Polygon polygon) throws XMLStreamException {
		final StrokeStyle strokeStyle = style.getStroke ();
		final FillStyle fillStyle = style.getFill ();
		if (strokeStyle == null && fillStyle == null) {
			return;
		}
		
		final LineString ring = polygon.getExteriorRing ();
		final List<SvgPoint> points = new ArrayList<> (ring.getNumPoints ());
		final int n = ring.getNumPoints ();
		
		for (int i = 0; i < n; ++ i) {
			points.add (createSvgPoint (ring.getPointN (i)));
		}
		
		super.polygon (writer, points, createStroke (strokeStyle), createFill (fillStyle));
	}
	
	public Fill createFill (final FillStyle fill) {
		if (fill == null) {
			return null;
		}
		
		return new Fill (
			String.format (
				Locale.US, 
				"#%02X%02X%02X", 
				(int)(fill.getColor ().getR () * 255.0) & 0xFF,
				(int)(fill.getColor ().getG () * 255.0) & 0xFF,
				(int)(fill.getColor ().getB () * 255.0) & 0xFF
			),
			fill.getColor ().getA ()
		);
	}
	
	public Stroke createStroke (final StrokeStyle stroke) {
		if (stroke == null) {
			return null;
		}
		
		return new Stroke (
			String.format (
				Locale.US, 
				"#%02X%02X%02X", 
				(int)(stroke.getColor ().getR () * 255.0) & 0xFF,
				(int)(stroke.getColor ().getG () * 255.0) & 0xFF,
				(int)(stroke.getColor ().getB () * 255.0) & 0xFF
			),
			stroke.getColor ().getA (),
			stroke.getWidth () * resolution
		);
	}
	
	public Fill makeSolid (final Fill fill) {
		if (fill == null) {
			return new Fill ("#ffffff", 1.0);
		}
		
		return new Fill (fill.getColor (), 1.0);
	}
	
	public Fill createFillFromStroke (final Stroke stroke, final Fill fill) {
		if (stroke == null) {
			return fill;
		}
		
		return new Fill (stroke.getColor(), stroke.getOpacity ());
	}
	
	public SvgPoint createSvgPoint (final Point point) {
		return createSvgPoint (point.getX (), point.getY ());
	}
	
	public SvgPoint createSvgPoint (final double x, final double y) {
		return new SvgPoint (x, envelope.getMaxY () - (y - envelope.getMinY ()));
	}
	
	public static class PositionedTextOverlay {
		private final Overlay overlay;
		private final SvgPoint anchor;
		private final SvgPoint position;
		private final SvgPoint size;
		
		public PositionedTextOverlay (final Overlay overlay, final SvgPoint anchor, final SvgPoint position, final SvgPoint size) {
			this.overlay = overlay;
			this.anchor = anchor;
			this.position = position;
			this.size = size;
		}

		public Overlay getOverlay () {
			return overlay;
		}

		public SvgPoint getAnchor () {
			return anchor;
		}

		public SvgPoint getPosition () {
			return position;
		}

		public SvgPoint getSize () {
			return size;
		}
	}
}
