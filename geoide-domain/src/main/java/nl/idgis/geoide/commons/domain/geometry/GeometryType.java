package nl.idgis.geoide.commons.domain.geometry;

import java.io.Serializable;

public abstract class GeometryType implements Serializable {
	private static final long serialVersionUID = -5138892514368052251L;
	
	public final static Type<Point> POINT = new PointType ();
	public final static Type<Curve> CURVE = new CurveType ();
	public final static Type<Surface> SURFACE = new SurfaceType ();
	public final static Type<GeometryCollection> GEOMETRY_COLLECTION = new GeometryCollectionType ();
	public final static Type<LineString> LINE_STRING = new LineStringType ();
	public final static Type<Polygon> POLYGON = new PolygonType ();
	public final static Type<MultiSurface> MULTI_SURFACE = new MultiSurfaceType ();
	public final static Type<MultiCurve> MULTI_CURVE = new MultiCurveType ();
	public final static Type<MultiPoint> MULTI_POINT = new MultiPointType ();
	public final static Type<Line> LINE = new LineType ();
	public final static Type<LinearRing> LINEAR_RING = new LinearRingType ();
	public final static Type<MultiPolygon> MULTI_POLYGON = new MultiPolygonType ();
	public final static Type<MultiLineString> MULTI_LINE_STRING = new MultiLineStringType ();
	
	public abstract Class<? extends Geometry> getGeometryClass ();
	public abstract String getName ();
	
	@Override
	public String toString () {
		return getName ();
	}
	
	public static abstract class Type<T extends Geometry> extends GeometryType {
		private static final long serialVersionUID = 2919199549308007529L;

		@Override
		public abstract Class<T> getGeometryClass ();
	}
	
	private final static class PointType extends Type<Point> {
		private static final long serialVersionUID = 3661614266522210576L;

		@Override
		public Class<Point> getGeometryClass () {
			return Point.class;
		}

		@Override
		public String getName () {
			return "Point";
		}
	}
	
	private final static class CurveType extends Type<Curve> {
		private static final long serialVersionUID = -2174349412054809383L;

		@Override
		public Class<Curve> getGeometryClass () {
			return Curve.class;
		}

		@Override
		public String getName () {
			return "Curve";
		}
	}
	
	private final static class SurfaceType extends Type<Surface> {
		private static final long serialVersionUID = 4706412438711007288L;

		@Override
		public Class<Surface> getGeometryClass () {
			return Surface.class;
		}

		@Override
		public String getName () {
			return "Surface";
		}
	}
	
	private final static class GeometryCollectionType extends Type<GeometryCollection> {
		private static final long serialVersionUID = 4447489802294816287L;

		@Override
		public Class<GeometryCollection> getGeometryClass () {
			return GeometryCollection.class;
		}

		@Override
		public String getName () {
			return "GeometryCollection";
		}
	}
	
	private final static class LineStringType extends Type<LineString> {
		private static final long serialVersionUID = -3672462699077712848L;

		@Override
		public Class<LineString> getGeometryClass () {
			return LineString.class;
		}

		@Override
		public String getName () {
			return "LineString";
		}
	}
	
	private final static class PolygonType extends Type<Polygon> {
		private static final long serialVersionUID = -6531194090717579714L;

		@Override
		public Class<Polygon> getGeometryClass () {
			return Polygon.class;
		}

		@Override
		public String getName () {
			return "Polygon";
		}
	}
	
	private final static class MultiSurfaceType extends Type<MultiSurface> {
		private static final long serialVersionUID = 4192688224693228110L;

		@Override
		public Class<MultiSurface> getGeometryClass () {
			return MultiSurface.class;
		}

		@Override
		public String getName () {
			return "MultiSurface";
		}
	}
	
	private final static class MultiCurveType extends Type<MultiCurve> {
		private static final long serialVersionUID = 2060453617015562569L;

		@Override
		public Class<MultiCurve> getGeometryClass () {
			return MultiCurve.class;
		}

		@Override
		public String getName () {
			return "MultiCurve";
		}
	}
	
	private final static class MultiPointType extends Type<MultiPoint> {
		private static final long serialVersionUID = -7027817683843685631L;

		@Override
		public Class<MultiPoint> getGeometryClass () {
			return MultiPoint.class;
		}

		@Override
		public String getName () {
			return "MultiPoint";
		}
	}
	
	private final static class LineType extends Type<Line> {
		private static final long serialVersionUID = 7302002518295948833L;

		@Override
		public Class<Line> getGeometryClass () {
			return Line.class;
		}

		@Override
		public String getName () {
			return "Line";
		}
	}
	
	private final static class LinearRingType extends Type<LinearRing> {
		private static final long serialVersionUID = -4555013791696283795L;

		@Override
		public Class<LinearRing> getGeometryClass () {
			return LinearRing.class;
		}

		@Override
		public String getName () {
			return "LinearRing";
		}
	}
	
	private final static class MultiPolygonType extends Type<MultiPolygon> {
		private static final long serialVersionUID = 5107793197124603383L;

		@Override
		public Class<MultiPolygon> getGeometryClass () {
			return MultiPolygon.class;
		}

		@Override
		public String getName () {
			return "MultiPolygon";
		}
	}
	
	private final static class MultiLineStringType extends Type<MultiLineString> {
		private static final long serialVersionUID = -7360742585100218399L;

		@Override
		public Class<MultiLineString> getGeometryClass () {
			return MultiLineString.class;
		}

		@Override
		public String getName () {
			return "MultiLineString";
		}
	}
}
