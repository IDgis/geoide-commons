package nl.idgis.geoide.commons.report.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.idgis.geoide.commons.domain.feature.FeatureOverlay;
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
		
		styledGeometries (writer, feature.getStyledGeometry ());
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
				(int)(fill.getColor ().getR () * 255.0),
				(int)(fill.getColor ().getG () * 255.0),
				(int)(fill.getColor ().getB () * 255.0)
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
				(int)(stroke.getColor ().getR () * 255.0),
				(int)(stroke.getColor ().getG () * 255.0),
				(int)(stroke.getColor ().getB () * 255.0)
			),
			stroke.getColor ().getA (),
			stroke.getWidth () * resolution
		);
	}
	
	public SvgPoint createSvgPoint (final Point point) {
		return new SvgPoint (point.getX (), envelope.getMaxY () - (point.getY () - envelope.getMinY ()));
	}
}
