package nl.idgis.planoview.commons.domain.geometry.wkb;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;

import nl.idgis.planoview.commons.domain.geometry.Envelope;
import nl.idgis.planoview.commons.domain.geometry.Geometry;
import nl.idgis.planoview.commons.domain.geometry.GeometryType;
import nl.idgis.planoview.commons.domain.geometry.GeometryType.Type;
import nl.idgis.planoview.commons.domain.geometry.GeometryCollection;
import nl.idgis.planoview.commons.domain.geometry.LineString;
import nl.idgis.planoview.commons.domain.geometry.LinearRing;
import nl.idgis.planoview.commons.domain.geometry.MultiLineString;
import nl.idgis.planoview.commons.domain.geometry.MultiPoint;
import nl.idgis.planoview.commons.domain.geometry.MultiPolygon;
import nl.idgis.planoview.commons.domain.geometry.Point;
import nl.idgis.planoview.commons.domain.geometry.Polygon;
import nl.idgis.planoview.commons.domain.geometry.Srs;

import com.fasterxml.jackson.annotation.JsonValue;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;

public class WkbGeometry implements Geometry {
	private static final long serialVersionUID = -804891431458689856L;
	
	private Srs srs;
	private transient com.vividsolutions.jts.geom.Geometry jtsGeometry;
	
	public WkbGeometry (final Srs srs, final byte[] wkb) {
		this (srs, parse (wkb));
	}
	
	private WkbGeometry (final Srs srs, final com.vividsolutions.jts.geom.Geometry jtsGeometry) {
		this.srs = srs;
		this.jtsGeometry = jtsGeometry;
	}
	
	@JsonValue
	public String jsonValue () {
		return asText ();
	}

	private static com.vividsolutions.jts.geom.Geometry parse (final byte[] wkb) {
		final WKBReader reader = new WKBReader ();
		try {
			return reader.read (wkb);
		} catch (ParseException e) {
			throw new IllegalArgumentException ("Invalid WKB geometry", e);
		}
	}
	
	private void writeObject (final ObjectOutputStream out) throws IOException {
		out.writeObject (srs);
		out.writeObject (asBytes ());
	}
	
	private void readObject (final ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.srs = (Srs) in.readObject ();
		this.jtsGeometry = parse ((byte[]) in.readObject ());
	}
	
	@SuppressWarnings("unused")
	private void readObjectNoData () throws ObjectStreamException {
		throw new InvalidObjectException ("Stream data required");
	}
	
	@Override
	public Srs getSrs () {
		return srs;
	}
	
	@Override
	public Geometry getEnvelope () {
		return new WkbGeometry (srs, jtsGeometry.getEnvelope ());
	}
	
	@Override
	public Envelope getRawEnvelope () {
		final com.vividsolutions.jts.geom.Envelope envelope = jtsGeometry.getEnvelopeInternal ();
		
		return new Envelope (
				envelope.getMinX (),
				envelope.getMinY (),
				envelope.getMaxX (),
				envelope.getMaxY ()
			);
	}

	@Override
	public GeometryType getType () {
		if (jtsGeometry instanceof com.vividsolutions.jts.geom.MultiLineString) {
			return GeometryType.MULTI_LINE_STRING;
		} else if (jtsGeometry instanceof com.vividsolutions.jts.geom.MultiPolygon) {
			return GeometryType.MULTI_POLYGON;
		} else if (jtsGeometry instanceof com.vividsolutions.jts.geom.LinearRing) {
			return GeometryType.LINEAR_RING;
		} else if (jtsGeometry instanceof com.vividsolutions.jts.geom.MultiPoint) {
			return GeometryType.MULTI_POINT;
		} else if (jtsGeometry instanceof com.vividsolutions.jts.geom.Polygon) {
			return GeometryType.POLYGON;
		} else if (jtsGeometry instanceof com.vividsolutions.jts.geom.LineString) {
			return GeometryType.LINE_STRING;
		} else if (jtsGeometry instanceof com.vividsolutions.jts.geom.GeometryCollection) {
			return GeometryType.GEOMETRY_COLLECTION;
		} else if (jtsGeometry instanceof com.vividsolutions.jts.geom.Point) {
			return GeometryType.POINT;
		}

		throw new IllegalStateException ("Unknown geometry type: " + jtsGeometry.getClass ().getCanonicalName ());
	}

	@Override
	public boolean is (final GeometryType type) {
		return type.getGeometryClass ().isAssignableFrom (getType ().getGeometryClass ());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Geometry> T as (final Type<T> type) {
		if (!is (type)) {
			return null;
		}
		
		if (type == GeometryType.MULTI_LINE_STRING) {
			return (T) new WrappedMultiLineString (this);
		} else if (type == GeometryType.MULTI_POLYGON) {
			return (T) new WrappedMultiPolygon (this);
		} else if (type == GeometryType.LINEAR_RING) {
			return (T) new WrappedLinearRing (this);
		} else if (type == GeometryType.MULTI_POINT) {
			return (T) new WrappedMultiPoint (this);
		} else if (type == GeometryType.POLYGON) {
			return (T) new WrappedPolygon (this);
		} else if (type == GeometryType.LINE_STRING) {
			return (T) new WrappedLineString (this);
		} else if (type == GeometryType.GEOMETRY_COLLECTION) {
			return (T) new WrappedGeometryCollection (this);
		} else if (type == GeometryType.POINT) {
			return (T) new WrappedPoint (this);
		} else {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Geometry as (final GeometryType type) {
		if (!is (type)) {
			return null;
		}
		
		return as ((GeometryType.Type) type);
	}
	
	@Override
	public byte[] asBytes () {
		return new WKBWriter ().write (jtsGeometry); 
	}

	@Override
	public String asText () {
		return new WKTWriter ().write (jtsGeometry);
	}
	
	@Override
	public String toString () {
		return asText ();
	}
	
	private static abstract class WrappedGeometry implements Geometry {
		private static final long serialVersionUID = -617003639662114767L;
		
		private final WkbGeometry geom;
		
		WrappedGeometry (final WkbGeometry geom) {
			this.geom = geom;
		}
		
		@Override
		public Srs getSrs () {
			return geom.getSrs ();
		}
		
		@Override
		public Geometry getEnvelope () {
			return geom.getEnvelope ();
		}
		
		@Override
		public Envelope getRawEnvelope () {
			return geom.getRawEnvelope ();
		}

		@Override
		public GeometryType getType () {
			return geom.getType ();
		}
		
		@Override
		public boolean is (final GeometryType type) {
			return type.getGeometryClass ().isAssignableFrom (getType ().getGeometryClass ());
		}

		@Override
		public <T extends Geometry> T as (final Type<T> type) {
			return geom.as (type);
		}
		
		@Override
		public Geometry as (final GeometryType type) {
			return geom.as (type);
		}
		
		@Override
		public byte[] asBytes () {
			return geom.asBytes ();
		}
		
		@Override
		public String asText () {
			return geom.asText ();
		}
		
		@Override
		public String toString () {
			return asText ();
		}
	}
	
	private final static class WrappedMultiLineString extends WrappedGeometry implements MultiLineString {
		private static final long serialVersionUID = 2799363581891733074L;

		WrappedMultiLineString (final WkbGeometry geom) {
			super(geom);
		}
		
	}
	
	private final static class WrappedMultiPolygon extends WrappedGeometry implements MultiPolygon {
		private static final long serialVersionUID = 8645892310447707657L;

		WrappedMultiPolygon (final WkbGeometry geom) {
			super(geom);
		}
	}
	
	private final static class WrappedLinearRing extends WrappedGeometry implements LinearRing {
		private static final long serialVersionUID = 8238969034890652386L;

		WrappedLinearRing (final WkbGeometry geom) {
			super(geom);
		}
	}
	
	private final static class WrappedMultiPoint extends WrappedGeometry implements MultiPoint {
		private static final long serialVersionUID = -1261739257169632027L;

		WrappedMultiPoint (final WkbGeometry geom) {
			super(geom);
		}
	}
	
	private final static class WrappedPolygon extends WrappedGeometry implements Polygon {
		private static final long serialVersionUID = -283187527729616785L;

		WrappedPolygon (final WkbGeometry geom) {
			super(geom);
		}
	}
	
	private final static class WrappedLineString extends WrappedGeometry implements LineString {
		private static final long serialVersionUID = -5908389269579388987L;

		WrappedLineString (final WkbGeometry geom) {
			super(geom);
		}
	}
	
	private final static class WrappedGeometryCollection extends WrappedGeometry implements GeometryCollection {
		private static final long serialVersionUID = -1370588734404539955L;

		WrappedGeometryCollection (final WkbGeometry geom) {
			super(geom);
		}
	}
	
	private final static class WrappedPoint extends WrappedGeometry implements Point {
		private static final long serialVersionUID = -2129996737754962889L;

		WrappedPoint (final WkbGeometry geom) {
			super(geom);
		}
	}
}
