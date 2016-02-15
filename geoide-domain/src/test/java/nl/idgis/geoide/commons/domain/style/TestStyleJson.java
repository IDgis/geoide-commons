package nl.idgis.geoide.commons.domain.style;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This test contains various asserts that verify whether styles can be parsed from JSON strings.
 */
public class TestStyleJson {

	private static ObjectMapper mapper;
	
	private final static String fillStyleValue = "{ \"color\": \"rgba(255, 255, 255, 1)\" }";
	private final static String strokeStyleValue = "{ \"color\": \"#ffffff\", \"lineDash\": null, \"width\": 1.25 }"; 
	private final static String imageStyleValue = "{ \"anchor\": [6.5, 6.5], \"fill\": { \"color\": \"rgba(255,255,255,1)\" }, \"opacity\": 1, \"origin\": [0, 0], \"radius\": 5, \"rotateWithView\": false, \"rotation\": 0, \"scale\": 1, \"size\": [13, 13], \"snapToPixel\": true, \"stroke\": { \"color\": \"#fff\", \"lineDash\": null, \"width\": 1.25 }, \"type\": \"circle\" }"; 
	private final static String textStyleValue = "null";
	
	@BeforeClass
	public static void createMapper () {
		mapper = new ObjectMapper ();
	}
	
	@Test
	public void testParseColor () {
		final Color color = new Color ("#010203");
		
		assertEquals (1.0 / 255.0, color.getR (), .0001);
		assertEquals (2.0 / 255.0, color.getG (), .0001);
		assertEquals (3.0 / 255.0, color.getB (), .0001);
		assertEquals (1.0, color.getA (), .0001);
		
		final Color color2 = new Color ("rgb(4, 5, 6)");
		
		assertEquals (4.0 / 255.0, color2.getR (), .0001);
		assertEquals (5.0 / 255.0, color2.getG (), .0001);
		assertEquals (6.0 / 255.0, color2.getB (), .0001);
		assertEquals (1.0, color2.getA (), .0001);
		
		final Color color3 = new Color ("rgba(7, 8, 9, 0.42)");
		
		assertEquals (7.0 / 255.0, color3.getR (), .0001);
		assertEquals (8.0 / 255.0, color3.getG (), .0001);
		assertEquals (9.0 / 255.0, color3.getB (), .0001);
		assertEquals (.42, color3.getA (), .0001);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidColor () {
		new Color ("rgb(4, 5, 6");
	}
	
	@Test
	public void testParseFillStyle () throws Throwable {
		final FillStyle fillStyle = mapper.readValue (fillStyleValue, FillStyle.class);
		
		assertFillStyle (fillStyle);
	}
	
	private static void assertFillStyle (final FillStyle fillStyle) {
		assertNotNull (fillStyle.getColor ());
		assertEquals (1.0, fillStyle.getColor ().getR (), .0001);
		assertEquals (1.0, fillStyle.getColor ().getG (), .0001);
		assertEquals (1.0, fillStyle.getColor ().getB (), .0001);
		assertEquals (1.0, fillStyle.getColor ().getA (), .0001);
	}
	
	@Test
	public void testParseStrokeStyle () throws Throwable {
		final StrokeStyle strokeStyle = mapper.readValue (strokeStyleValue, StrokeStyle.class);
		
		assertStrokeStyle (strokeStyle);
	}
	
	private static void assertStrokeStyle (final StrokeStyle strokeStyle) {
		assertNotNull (strokeStyle.getColor ());
		assertEquals (1.0, strokeStyle.getColor ().getR (), .0001);
		assertEquals (1.0, strokeStyle.getColor ().getG (), .0001);
		assertEquals (1.0, strokeStyle.getColor ().getB (), .0001);
		assertEquals (1.0, strokeStyle.getColor ().getA (), .0001);
		
		assertNull (strokeStyle.getLineDash ());
		
		assertEquals (1.25, strokeStyle.getWidth (), .0001);
	}
	
	@Test
	public void testParseImageStyle () throws Throwable {
		final ImageStyle imageStyle = mapper.readValue (imageStyleValue, ImageStyle.class);
		
		assertImageStyle (imageStyle);
	}
	
	private static void assertImageStyle (final ImageStyle imageStyle) {
		assertNotNull (imageStyle.getFill ());
		assertEquals (1.0, imageStyle.getFill ().getColor ().getR (), .0001);
		assertEquals (1.0, imageStyle.getFill ().getColor ().getG (), .0001);
		assertEquals (1.0, imageStyle.getFill ().getColor ().getB (), .0001);
		assertEquals (1.0, imageStyle.getFill ().getColor ().getA (), .0001);
		
		assertEquals (1.0, imageStyle.getOpacity (), .0001);
		
		assertFalse (imageStyle.getRotateWithView ());
		
		assertEquals (0, imageStyle.getRotation (), .0001);
		
		assertEquals (5, imageStyle.getRadius (), .0001);
		
		assertEquals (1, imageStyle.getScale (), .0001);
		
		assertTrue (imageStyle.getSnapToPixel ());
		
		assertEquals ("circle", imageStyle.getType ());
		
		assertNotNull (imageStyle.getStroke ());
		assertEquals (1.0, imageStyle.getStroke ().getColor ().getR (), .0001);
		assertEquals (1.0, imageStyle.getStroke ().getColor ().getG (), .0001);
		assertEquals (1.0, imageStyle.getStroke ().getColor ().getB (), .0001);
		assertEquals (1.0, imageStyle.getStroke ().getColor ().getA (), .0001);
		assertNull (imageStyle.getStroke ().getLineDash ());
		assertEquals (1.25, imageStyle.getStroke ().getWidth (), .0001);
	}
	
	@Test
	public void testPareStyle () throws Throwable {
		final Style style = mapper.readValue ("{ \"fill\": " + fillStyleValue + ", \"image\": " + imageStyleValue + ", \"stroke\": " + strokeStyleValue + ", \"text\": " + textStyleValue + ", \"zIndex\": 1 }", Style.class);
		
		assertNotNull (style.getFill ());
		assertFillStyle (style.getFill ());
		
		assertNotNull (style.getStroke ());
		assertStrokeStyle (style.getStroke ());
		
		assertNotNull (style.getImage ());
		assertImageStyle (style.getImage ());
		
		assertNull (style.getText ());
	}
}
