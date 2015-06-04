package nl.idgis.ogc.client.wfs;

import java.io.InputStream;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.ogc.wfs.WFSCapabilities;
import nl.idgis.ogc.wfs.WFSCapabilities.OperationType;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestWFSCapabilitiesParser {

	public @Test void testWfs110 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wfs/wfs-response-1.1.0.xml");

		final WFSCapabilities capabilities = WFSCapabilitiesParser.parseCapabilities (is);
		
		is.close ();
		
		assertCapabilities (capabilities, "1.1.0");
	}
	
	public @Test void testWfs200 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wfs/wfs-response-2.0.0.xml");

		final WFSCapabilities capabilities = WFSCapabilitiesParser.parseCapabilities (is);
		
		is.close ();
		
		assertCapabilities (capabilities, "2.0.0");
	}
	
	public @Test void testWfs110Geoserver () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wfs/wfs-capabilities-1.1.0-geoserver.xml");
		
		final WFSCapabilities capabilities = WFSCapabilitiesParser.parseCapabilities (is);
		
		is.close ();
		
		assertEquals ("1.1.0", capabilities.version ());
		
		assertNotNull (capabilities.operation (OperationType.GET_FEATURE));
	}
	
	public static void assertCapabilities (final WFSCapabilities capabilities, final String version) {
		assertEquals (version, capabilities.version ());
		
		assertEquals ("INSPIRE Download service voor Beschermde Gebieden van de gezamenlijke provincies", capabilities.serviceIdentification ().title ());
		assertEquals ("Deze Download service is gebaseerd op de geharmoniseerde provinciale datasets voor Beschermde Gebieden. Onderdeel van deze Download service zijn datasets voor Aardkundige Waarden, Ecologische Hoofdstructuur, Provinciale Monumenten, Nationale Landschappen, Stiltegebieden en WAV gebieden", capabilities.serviceIdentification ().abstractText ());
		
		assertEquals (3, capabilities.operations ().size ());
		assertNotNull (capabilities.operation (OperationType.DESCRIBE_FEATURE_TYPE));
		assertNotNull (capabilities.operation (OperationType.GET_CAPABILITIES));
		assertNotNull (capabilities.operation (OperationType.GET_FEATURE));
		assertEquals ("http://services.inspire-provincies.nl/ProtectedSites/services/download_PS?", capabilities.operation (OperationType.DESCRIBE_FEATURE_TYPE).httpGet ());
		assertEquals ("http://services.inspire-provincies.nl/ProtectedSites/services/download_PS", capabilities.operation (OperationType.DESCRIBE_FEATURE_TYPE).httpPost ());
		assertEquals ("http://services.inspire-provincies.nl/ProtectedSites/services/download_PS?", capabilities.operation (OperationType.GET_CAPABILITIES).httpGet ());
		assertEquals ("http://services.inspire-provincies.nl/ProtectedSites/services/download_PS", capabilities.operation (OperationType.GET_CAPABILITIES).httpPost ());
		assertEquals ("http://services.inspire-provincies.nl/ProtectedSites/services/download_PS?", capabilities.operation (OperationType.GET_FEATURE).httpGet ());
		assertEquals ("http://services.inspire-provincies.nl/ProtectedSites/services/download_PS", capabilities.operation (OperationType.GET_FEATURE).httpPost ());
		
		assertEquals (2, capabilities.featureTypes ().size ());
		assertNotNull (capabilities.featureType ("urn:x-inspire:specification:gmlas:ProtectedSites:3.0", "ProtectedSite"));
		assertNotNull (capabilities.featureType ("urn:x-inspire:specification:gmlas:GeographicalNames:3.0", "NamedPlace"));
		
		assertEquals ("gn:NamedPlace", capabilities.featureType ("urn:x-inspire:specification:gmlas:GeographicalNames:3.0", "NamedPlace").title ());
		assertEquals ("gn", capabilities.featureType ("urn:x-inspire:specification:gmlas:GeographicalNames:3.0", "NamedPlace").namespacePrefix ());
		assertEquals ("urn:ogc:def:crs:EPSG::28992", capabilities.featureType ("urn:x-inspire:specification:gmlas:GeographicalNames:3.0", "NamedPlace").crs ());
		assertTrue (capabilities.featureType ("urn:x-inspire:specification:gmlas:GeographicalNames:3.0", "NamedPlace").otherCrs ().contains ("urn:ogc:def:crs:EPSG::4326"));
		assertTrue (capabilities.featureType ("urn:x-inspire:specification:gmlas:GeographicalNames:3.0", "NamedPlace").supports (new MimeContentType ("application/gml+xml; version=3.2")));
		
		assertEquals ("ps:ProtectedSite", capabilities.featureType ("urn:x-inspire:specification:gmlas:ProtectedSites:3.0", "ProtectedSite").title ());
		assertEquals ("urn:ogc:def:crs:EPSG::28992", capabilities.featureType ("urn:x-inspire:specification:gmlas:ProtectedSites:3.0", "ProtectedSite").crs ());
		assertEquals ("ps", capabilities.featureType ("urn:x-inspire:specification:gmlas:ProtectedSites:3.0", "ProtectedSite").namespacePrefix ());
		assertTrue (capabilities.featureType ("urn:x-inspire:specification:gmlas:ProtectedSites:3.0", "ProtectedSite").otherCrs ().contains ("urn:ogc:def:crs:EPSG::4326"));
		assertTrue (capabilities.featureType ("urn:x-inspire:specification:gmlas:ProtectedSites:3.0", "ProtectedSite").supports (new MimeContentType ("application/gml+xml; version=3.2")));
	}
}
