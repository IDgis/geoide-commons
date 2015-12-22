package nl.idgis.geoide.commons.domain.geometry.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryCollection;
import nl.idgis.geoide.commons.domain.geometry.LineString;
import nl.idgis.geoide.commons.domain.geometry.MultiLineString;
import nl.idgis.geoide.commons.domain.geometry.MultiPoint;
import nl.idgis.geoide.commons.domain.geometry.MultiPolygon;
import nl.idgis.geoide.commons.domain.geometry.Point;
import nl.idgis.geoide.commons.domain.geometry.Polygon;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This test includes asserts to test whether the various types of GeoJSON entities
 * can be parsed from JSON-strings using Jackson. One for each geometry type.
 */
public class TestGeoJsonGeometry {

	@Test
	public void testParsePoint () throws Throwable {
		final Point point = parse ("{ \"type\": \"Point\", \"coordinates\": [100.0, 0.0] }", Point.class);
		
		assertEquals (100.0, point.getX (), .0001);
		assertEquals (0.0, point.getY (), .0001);
	}
	
	@Test
	public void testParseMultiPoint () throws Throwable {
		final MultiPoint multiPoint = parse ("{ \"type\": \"MultiPoint\", \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ] }", MultiPoint.class);
		
		assertEquals (2, multiPoint.getNumGeometries ());
		assertEquals (100.0, multiPoint.getGeometryN (0).getX (), .0001);
		assertEquals (0.0, multiPoint.getGeometryN (0).getY (), .0001);
		assertEquals (101.0, multiPoint.getGeometryN (1).getX (), .0001);
		assertEquals (1.0, multiPoint.getGeometryN (1).getY (), .0001);
	}
	
	@Test
	public void testParseLineString () throws Throwable {
		final LineString lineString = parse ("{ \"type\": \"LineString\", \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ] }", LineString.class);

		assertEquals (2, lineString.getNumPoints ());
		
		assertEquals (100.0, lineString.getPointN (0).getX (), .0001);
		assertEquals (0.0, lineString.getPointN (0).getY (), .0001);
		assertEquals (101.0, lineString.getPointN (1).getX (), .0001);
		assertEquals (1.0, lineString.getPointN (1).getY (), .0001);
	}
	
	@Test
	public void testParseMultiLineString () throws Throwable {
		final MultiLineString multiLineString = parse ("{ \"type\": \"MultiLineString\", \"coordinates\": [ [ [100.0, 0.0], [101.0, 1.0] ], [ [102.0, 2.0], [103.0, 3.0] ] ] }", MultiLineString.class);
		
		assertEquals (2, multiLineString.getNumGeometries ());
		
		assertEquals (100.0, multiLineString.getGeometryN (0).getPointN (0).getX (), .0001);
		assertEquals (0.0, multiLineString.getGeometryN (0).getPointN (0).getY (), .0001);
		assertEquals (101.0, multiLineString.getGeometryN (0).getPointN (1).getX (), .0001);
		assertEquals (1.0, multiLineString.getGeometryN (0).getPointN (1).getY (), .0001);
		
		assertEquals (102.0, multiLineString.getGeometryN (1).getPointN (0).getX (), .0001);
		assertEquals (2.0, multiLineString.getGeometryN (1).getPointN (0).getY (), .0001);
		assertEquals (103.0, multiLineString.getGeometryN (1).getPointN (1).getX (), .0001);
		assertEquals (3.0, multiLineString.getGeometryN (1).getPointN (1).getY (), .0001);
	}
	
	
	@Test
	public void testParsePolygon () throws Throwable {
		final Polygon polygon = parse ("{ \"type\": \"Polygon\", \"coordinates\": [ [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ] ] }", Polygon.class);

		assertEquals (0, polygon.getNumInteriorRing ());

		assertEquals (5, polygon.getExteriorRing ().getNumPoints ());
		assertEquals (100.0, polygon.getExteriorRing ().getPointN (0).getX (), .0001);
		assertEquals (0.0, polygon.getExteriorRing ().getPointN (0).getY (), .0001);
		assertEquals (101.0, polygon.getExteriorRing ().getPointN (1).getX (), .0001);
		assertEquals (0.0, polygon.getExteriorRing ().getPointN (1).getY (), .0001);
		assertEquals (101.0, polygon.getExteriorRing ().getPointN (2).getX (), .0001);
		assertEquals (1.0, polygon.getExteriorRing ().getPointN (2).getY (), .0001);
		assertEquals (100.0, polygon.getExteriorRing ().getPointN (3).getX (), .0001);
		assertEquals (1.0, polygon.getExteriorRing ().getPointN (3).getY (), .0001);
		assertEquals (100.0, polygon.getExteriorRing ().getPointN (4).getX (), .0001);
		assertEquals (0.0, polygon.getExteriorRing ().getPointN (4).getY (), .0001);
	}
	
	@Test
	public void testParsePolygonWithInteriorRing () throws Throwable {
		final Polygon polygon = parse ("{ \"type\": \"Polygon\", \"coordinates\": [ [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ], [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ] ] }", Polygon.class);
		
		assertEquals (1, polygon.getNumInteriorRing ());

		assertEquals (5, polygon.getExteriorRing ().getNumPoints ());
		assertEquals (100.0, polygon.getExteriorRing ().getPointN (0).getX (), .0001);
		assertEquals (0.0, polygon.getExteriorRing ().getPointN (0).getY (), .0001);
		assertEquals (101.0, polygon.getExteriorRing ().getPointN (1).getX (), .0001);
		assertEquals (0.0, polygon.getExteriorRing ().getPointN (1).getY (), .0001);
		assertEquals (101.0, polygon.getExteriorRing ().getPointN (2).getX (), .0001);
		assertEquals (1.0, polygon.getExteriorRing ().getPointN (2).getY (), .0001);
		assertEquals (100.0, polygon.getExteriorRing ().getPointN (3).getX (), .0001);
		assertEquals (1.0, polygon.getExteriorRing ().getPointN (3).getY (), .0001);
		assertEquals (100.0, polygon.getExteriorRing ().getPointN (4).getX (), .0001);
		assertEquals (0.0, polygon.getExteriorRing ().getPointN (4).getY (), .0001);
		
		assertEquals (5, polygon.getInteriorRingN (0).getNumPoints ());
		assertEquals (100.2, polygon.getInteriorRingN (0).getPointN (0).getX (), .0001);
		assertEquals (0.2, polygon.getInteriorRingN (0).getPointN (0).getY (), .0001);
		assertEquals (100.8, polygon.getInteriorRingN (0).getPointN (1).getX (), .0001);
		assertEquals (0.2, polygon.getInteriorRingN (0).getPointN (1).getY (), .0001);
		assertEquals (100.8, polygon.getInteriorRingN (0).getPointN (2).getX (), .0001);
		assertEquals (0.8, polygon.getInteriorRingN (0).getPointN (2).getY (), .0001);
		assertEquals (100.2, polygon.getInteriorRingN (0).getPointN (3).getX (), .0001);
		assertEquals (0.8, polygon.getInteriorRingN (0).getPointN (3).getY (), .0001);
		assertEquals (100.2, polygon.getInteriorRingN (0).getPointN (4).getX (), .0001);
		assertEquals (0.2, polygon.getInteriorRingN (0).getPointN (4).getY (), .0001);
	}
	
	@Test
	public void testParseMultiPolygon () throws Throwable {
		final MultiPolygon multiPolygon = parse ("{ \"type\": \"MultiPolygon\", \"coordinates\": [ [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]], [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]], [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]] ] }", MultiPolygon.class);
		
		assertEquals (2, multiPolygon.getNumGeometries ());
		
		assertEquals (5, multiPolygon.getGeometryN (0).getExteriorRing ().getNumPoints ());
		assertEquals (102.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (0).getX (), .0001);
		assertEquals (2.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (0).getY (), .0001);
		assertEquals (103.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (1).getX (), .0001);
		assertEquals (2.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (1).getY (), .0001);
		assertEquals (103.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (2).getX (), .0001);
		assertEquals (3.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (2).getY (), .0001);
		assertEquals (102.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (3).getX (), .0001);
		assertEquals (3.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (3).getY (), .0001);
		assertEquals (102.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (4).getX (), .0001);
		assertEquals (2.0, multiPolygon.getGeometryN (0).getExteriorRing ().getPointN (4).getY (), .0001);
		
		assertEquals (5, multiPolygon.getGeometryN (1).getExteriorRing ().getNumPoints ());
		assertEquals (100.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (0).getX (), .0001);
		assertEquals (0.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (0).getY (), .0001);
		assertEquals (101.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (1).getX (), .0001);
		assertEquals (0.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (1).getY (), .0001);
		assertEquals (101.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (2).getX (), .0001);
		assertEquals (1.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (2).getY (), .0001);
		assertEquals (100.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (3).getX (), .0001);
		assertEquals (1.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (3).getY (), .0001);
		assertEquals (100.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (4).getX (), .0001);
		assertEquals (0.0, multiPolygon.getGeometryN (1).getExteriorRing ().getPointN (4).getY (), .0001);
		
		assertEquals (5, multiPolygon.getGeometryN (1).getInteriorRingN (0).getNumPoints ());
		assertEquals (100.2, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (0).getX (), .0001);
		assertEquals (0.2, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (0).getY (), .0001);
		assertEquals (100.8, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (1).getX (), .0001);
		assertEquals (0.2, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (1).getY (), .0001);
		assertEquals (100.8, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (2).getX (), .0001);
		assertEquals (0.8, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (2).getY (), .0001);
		assertEquals (100.2, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (3).getX (), .0001);
		assertEquals (0.8, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (3).getY (), .0001);
		assertEquals (100.2, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (4).getX (), .0001);
		assertEquals (0.2, multiPolygon.getGeometryN (1).getInteriorRingN (0).getPointN (4).getY (), .0001);
	}
	
	
	@Test
	public void testParseGeometryCollection () throws Throwable {
		final GeometryCollection<Geometry> geometryCollection = parse ("{ \"type\": \"GeometryCollection\", \"geometries\": [ { \"type\": \"Point\", \"coordinates\": [100.0, 0.0] }, { \"type\": \"LineString\", \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ] } ] }", GeometryCollection.class);

		assertEquals (2, geometryCollection.getNumGeometries ());
		assertTrue (geometryCollection.getGeometryN (0) instanceof Point);
		assertTrue (geometryCollection.getGeometryN (1) instanceof LineString);
		
		assertEquals (100.0, ((Point) geometryCollection.getGeometryN (0)).getX (), .0001);
		assertEquals (0.0, ((Point) geometryCollection.getGeometryN (0)).getY (), .0001);
		
		assertEquals (101.0, ((LineString) geometryCollection.getGeometryN (1)).getStartPoint ().getX (), .0001);
		assertEquals (0.0, ((LineString) geometryCollection.getGeometryN (1)).getStartPoint ().getY (), .0001);
		assertEquals (102.0, ((LineString) geometryCollection.getGeometryN (1)).getEndPoint ().getX (), .0001);
		assertEquals (1.0, ((LineString) geometryCollection.getGeometryN (1)).getEndPoint ().getY (), .0001);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T parse (final String geometry, final Class<? extends Geometry> expectedClass) throws JsonParseException, JsonMappingException, IOException {
		final ObjectMapper mapper = new ObjectMapper ();
		
		final Geometry geom = mapper.readValue (mapper.writeValueAsString (mapper.readValue (geometry, AbstractGeoJsonGeometry.class)), AbstractGeoJsonGeometry.class);
		
		assertTrue (expectedClass.isAssignableFrom (geom.getClass ()));
		
		return (T) geom;
	}
}
