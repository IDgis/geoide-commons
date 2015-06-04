package nl.idgis.ogc.client.wms;

import static org.junit.Assert.*;

import java.io.InputStream;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.ogc.wms.WMSCapabilities;
import nl.idgis.ogc.wms.WMSCapabilities.RequestType;

import org.junit.Test;

public class TestWMSCapabilitiesParser {

	public @Test void testWms111 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wms/wms-response-1.1.1-ro-online.xml");
		final WMSCapabilities c = WMSCapabilitiesParser.parseCapabilities (is);
		
		assertCapabilities (c);

		// Service:
		assertEquals ("OGC:WMS", c.service ().name ());
		assertEquals ("RO-Online: De landelijke voorziening voor digitale ruimtelijke ordeningsplannen", c.service ().title ());
		assertEquals ("Digitale ruimtelijke plannen die verplicht beschikbaar worden gesteld a.g.v. digitale aspecten van de nieuwe Wet ruimtelijke ordening (per 1/1/2010) en vrijwillig beschikbaar gesteld oudere plannen.", c.service ().abstractText ());
		assertTrue (c.service ().keywords().contains ("ruimtelijke ordening"));
		assertTrue (c.service ().keywords().contains ("IMRO"));
		
		// Request:
		assertTrue (c.operation (RequestType.GET_CAPABILITIES).formats ().contains (new MimeContentType ("application/vnd.ogc.wms_xml")));
		assertFalse (c.operation (RequestType.GET_CAPABILITIES).formats ().contains (new MimeContentType ("image/png")));
		assertEquals ("http://afnemers.ruimtelijkeplannen.nl/afnemers/services?", c.operation (RequestType.GET_CAPABILITIES).httpGet ());
		assertFalse (c.operation (RequestType.GET_MAP).formats ().contains (new MimeContentType ("application/vnd.ogc.wms_xml")));
		assertTrue (c.operation (RequestType.GET_MAP).formats ().contains (new MimeContentType ("image/png")));
		assertEquals ("http://afnemers.ruimtelijkeplannen.nl/afnemers/services?", c.operation (RequestType.GET_MAP).httpGet ());
		assertNotNull (c.operationByName ("GetMap"));
		
		// Layers:
		assertNotNull (c.layer ("XGB:Gerechtelijkeuitspraakgebied"));
		assertNotNull (c.layer ("NP:NationaalPlangebied"));
		assertNotNull (c.layer ("NP:NationaalVerbinding"));
		assertNotNull (c.layer ("NP:NationaalGebied"));
		
		assertTrue (c.layer ("NP:NationaalGebied").queryable ());
		assertEquals ("NP:NationaalGebied", c.layer ("NP:NationaalGebied").title ());
		assertTrue (c.layer ("NP:NationaalGebied").crss ().contains ("EPSG:28992"));
		assertEquals (-59874.0, c.layer ("NP:NationaalGebied").boundingBox ("EPSG:28992").minX (), 0.0001);
		assertEquals (249043.0, c.layer ("NP:NationaalGebied").boundingBox ("EPSG:28992").minY (), 0.0001);
		assertEquals (316878.0, c.layer ("NP:NationaalGebied").boundingBox ("EPSG:28992").maxX (), 0.0001);
		assertEquals (885500.0, c.layer ("NP:NationaalGebied").boundingBox ("EPSG:28992").maxY (), 0.0001);
		assertEquals ("NP:NationaalGebied", c.layer ("NP:NationaalGebied").style ("NP:NationaalGebied").title ());
	}
	
	public @Test void testWms130 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wms/wms-response-1.3.0-mapserver.xml");
		final WMSCapabilities c = WMSCapabilitiesParser.parseCapabilities (is);

		assertCapabilities (c);
		
		// Service:
		assertEquals ("WMS", c.service ().name ());
		assertEquals ("Bestemmingsplan", c.service ().title ());
		assertNull (c.service ().abstractText ());
		assertTrue (c.service ().keywords ().isEmpty ());
		
		// Request:
		assertTrue (c.operation (RequestType.GET_CAPABILITIES).formats ().contains (new MimeContentType ("text/xml")));
		assertFalse (c.operation (RequestType.GET_CAPABILITIES).formats ().contains (new MimeContentType ("image/png")));
		assertEquals ("https://geoproxy.tercera-ro.nl/9928_ev/proxy?map=%2Fmapserver%2Fms4w%2FApache%2Fhtdocs%2F9928%2FNLIMRO9928DOSx2011x0000029IP-VA01_rp.map&=wms&", c.request (RequestType.GET_CAPABILITIES).httpGet ());
		assertFalse (c.operation (RequestType.GET_MAP).formats ().contains (new MimeContentType ("text/xml")));
		assertTrue (c.operation (RequestType.GET_MAP).formats ().contains (new MimeContentType ("image/png")));
		assertEquals ("https://geoproxy.tercera-ro.nl/9928_ev/proxy?map=%2Fmapserver%2Fms4w%2FApache%2Fhtdocs%2F9928%2FNLIMRO9928DOSx2011x0000029IP-VA01_rp.map&=wms&", c.request (RequestType.GET_MAP).httpGet ());

		// Layers:
		assertNotNull (c.layer ("f_f24"));
		assertNotNull (c.layer ("b_32"));
		
		assertTrue (c.layer ("b_32").queryable ());
		assertEquals ("bouwvlak", c.layer ("b_32").title ());
		assertTrue (c.layer ("b_32").crss ().contains ("EPSG:28992"));
		assertEquals (97754.4, c.layer ("b_32").boundingBox ("EPSG:28992").minX (), 0.0001);
		assertEquals (430285, c.layer ("b_32").boundingBox ("EPSG:28992").minY (), 0.0001);
		assertEquals (99505.1, c.layer ("b_32").boundingBox ("EPSG:28992").maxX (), 0.0001);
		assertEquals (431667, c.layer ("b_32").boundingBox ("EPSG:28992").maxY (), 0.0001);
		assertEquals ("default", c.layer ("b_32").style ("default").title ());
		
		final WMSCapabilities.Layer enkelbestemming = c.layer ("enkelbestemming");
		assertNotNull (enkelbestemming);
		assertNotNull (c.layer ("e_e7"));
		assertNotNull (c.layer ("e_e12"));
		assertNotNull (c.layer ("e_e11"));
		assertNotNull (c.layer ("e_e9"));
		assertNotNull (c.layer ("e_e8"));
		assertNotNull (c.layer ("e_e13"));
		assertNotNull (c.layer ("e_e10"));
		assertEquals (7, enkelbestemming.layers ().size ());
	}
	
	private static void assertCapabilities (final WMSCapabilities c) {
		assertNotNull (c);
		assertFalse (c.layers ().isEmpty ());
		assertNotNull (c.service ());
		assertFalse (c.exceptionFormats ().isEmpty ());
		assertFalse (c.requests ().isEmpty ());
		
		assertTrue (c.hasRequestType (RequestType.GET_MAP));
		assertTrue (c.hasRequestType (RequestType.GET_CAPABILITIES));
		assertTrue (c.hasRequestType (RequestType.GET_FEATURE_INFO));
		assertTrue (c.hasRequestType (RequestType.GET_LEGEND_GRAPHIC));
	}

}
