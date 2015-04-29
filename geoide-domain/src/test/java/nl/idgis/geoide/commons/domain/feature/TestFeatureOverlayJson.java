package nl.idgis.geoide.commons.domain.feature;

import static org.junit.Assert.*;
import nl.idgis.geoide.commons.domain.geometry.Point;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestFeatureOverlayJson {
	
	private static ObjectMapper mapper;
	
	private final static String overlayValue = "{\"width\":150,\"height\":32,\"offset\":[-318,18],\"borderWidth\":2,\"arrowWidth\":20,\"arrowLength\":20,\"arrowDistance\":8,\"text\":\"Hello, World!\"}";
	private final static String styledGeometryValue = "{\"geometry\":{\"type\":\"Point\",\"coordinates\":[151696.12795898438,449427.4559179687]},\"style\":{\"fill\":{\"color\":\"rgba(255,255,255,0.4)\"},\"stroke\":{\"color\":\"#3399CC\",\"lineDash\":null,\"width\":1.25},\"image\":{\"opacity\":1,\"rotateWithView\":false,\"rotation\":0,\"scale\":1,\"snapToPixel\":true,\"anchor\":[6.5,6.5],\"origin\":[0,0],\"size\":[13,13],\"fill\":{\"color\":\"rgba(255,255,255,0.4)\"},\"radius\":5,\"type\":\"circle\",\"stroke\":{\"color\":\"#3399CC\",\"lineDash\":null,\"width\":1.25}},\"text\":null}}";
	private final static String overlayFeatureValue = "{\"overlay\":{\"width\":150,\"height\":32,\"offset\":[-318,18],\"borderWidth\":2,\"arrowWidth\":20,\"arrowLength\":20,\"arrowDistance\":8,\"text\":\"Hello, World!\"},\"styledGeometry\":[{\"geometry\":{\"type\":\"Point\",\"coordinates\":[151696.12795898438,449427.4559179687]},\"style\":{\"fill\":{\"color\":\"rgba(255,255,255,0.4)\"},\"stroke\":{\"color\":\"#3399CC\",\"lineDash\":null,\"width\":1.25},\"image\":{\"opacity\":1,\"rotateWithView\":false,\"rotation\":0,\"scale\":1,\"snapToPixel\":true,\"anchor\":[6.5,6.5],\"origin\":[0,0],\"size\":[13,13],\"fill\":{\"color\":\"rgba(255,255,255,0.4)\"},\"radius\":5,\"type\":\"circle\",\"stroke\":{\"color\":\"#3399CC\",\"lineDash\":null,\"width\":1.25}},\"text\":null}}]}";
	
	@BeforeClass
	public static void createMapper () {
		mapper = new ObjectMapper ();
	}
	
	@Test
	public void testParseOverlay () throws Throwable {
		final Overlay overlay = mapper.readValue (overlayValue, Overlay.class);
		
		assertOverlay (overlay);
	}
	
	private static void assertOverlay (final Overlay overlay) {
		assertEquals (150.0, overlay.getWidth (), .0001);
		assertEquals (32.0, overlay.getHeight (), .0001);
		assertNotNull (overlay.getOffset ());
		assertEquals (-318, overlay.getOffset ().get (0), .0001);
		assertEquals (18, overlay.getOffset ().get (1), .0001);
		assertEquals (2.0, overlay.getBorderWidth (), .0001);
		assertEquals (20.0, overlay.getArrowWidth (), .0001);
		assertEquals (20.0, overlay.getArrowLength (), .0001);
		assertEquals (8.0, overlay.getArrowDistance (), .0001);
		assertEquals ("Hello, World!", overlay.getText ());
	}
	
	@Test
	public void testParseStyledGeometry () throws Throwable {
		final StyledGeometry styledGeometry = mapper.readValue (styledGeometryValue, StyledGeometry.class);
		
		assertStyledGeometry (styledGeometry);
	}
	
	private static void assertStyledGeometry (final StyledGeometry styledGeometry) {
		assertNotNull (styledGeometry.getGeometry ());
		assertNotNull (styledGeometry.getStyle ());
		assertNotNull (styledGeometry.getStyle ().getFill ());
		assertNotNull (styledGeometry.getStyle ().getStroke ());
		assertNotNull (styledGeometry.getStyle ().getImage ());
		
		assertTrue (styledGeometry.getGeometry () instanceof Point);
	}
	
	@Test
	public void testParseOverlayFeature () throws Throwable {
		final OverlayFeature overlayFeature = mapper.readValue (overlayFeatureValue, OverlayFeature.class);

		assertNotNull (overlayFeature.getOverlay ());
		assertNotNull (overlayFeature.getStyledGeometry ());
		assertEquals (1, overlayFeature.getStyledGeometry ().size ());
		
		assertOverlay (overlayFeature.getOverlay ());
		
		assertStyledGeometry (overlayFeature.getStyledGeometry ().get (0));
	}
}
