package nl.idgis.geoide.commons.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class TestJsonFactory {

	private final static String mapDefinitionJson = 
		"{ \"services\": ["
				+ "{ \"id\": \"service-1\", \"label\": \"Service 1\", \"identification\": { \"serviceType\":\"WMS\", \"serviceEndpoint\":\"http://serviceendpoint.com\", \"serviceVersion\":\"1.0.0\" } },"
				+ "{ \"id\": \"service-2\", \"label\": \"Service 2\", \"identification\": { \"serviceType\":\"WMS\", \"serviceEndpoint\":\"http://serviceendpoint.com\", \"serviceVersion\":\"1.0.0\" } }," 
				+ "{ \"id\": \"service-3\", \"label\": \"Service 3\", \"identification\": { \"serviceType\":\"WFS\", \"serviceEndpoint\":\"http://serviceendpoint.com\", \"serviceVersion\":\"1.0.0\" } }" 
			+ "], "
			+ "\"serviceLayers\": ["
				+ "{ \"id\": \"service-layer-1\", \"label\": \"Service layer 1\", \"name\": \"layername\", \"service\": \"service-1\" },"
				+ "{ "
					+ "\"id\": \"service-layer-2\", "
					+ "\"label\": \"Service layer 2\", "
					+ "\"name\": \"layername\", "
					+ "\"service\": \"service-2\", "
					+ "\"featureType\": \"feature-type-1\" "
				+ "}"
			+ "], "
			+ "\"featureTypes\": ["
				+ "{ \"id\": \"feature-type-1\", \"label\": \"Feature type 1\", \"name\": \"featureTypeName\", \"service\": \"service-3\" } "
			+ "], "
			+ "\"layers\": ["
				+ "{"
					+ "\"id\": \"layer-2\", "
					+ "\"layerType\": \"default\", "
					+ "\"serviceLayers\": [\"service-layer-1\"] "
				+ "} ,"
				+ "{"
					+ "\"id\": \"layer-3\", "
					+ "\"layerType\": \"default\", "
					+ "\"serviceLayers\": [\"service-layer-2\"]"
				+ "}"
			+ "],"
			+ "\"maps\": ["
				+ "{"
					+ "\"id\": \"mapdef-1\", "
					+ "\"label\": \"Map definition 1\", "
					+ "\"maplayers\": ["
						+ "{"
							+ "\"id\": \"map-layer-1\", "
							+ "\"label\": \"Layer 1\", "
							+ "\"maplayers\": ["
								+ "{"
									+ "\"id\": \"map-layer-2\", "
									+ "\"layer\": \"layer-2\", "
									+ "\"label\": \"Layer 2\" "
								+ "}, "
								+ "{"
									+ "\"id\": \"map-layer-3\", "
									+ "\"layer\": \"layer-3\", "
									+ "\"label\": \"Layer 3\" "
								+ "}"
							+ "]"		
						+ "}"				
					+ "]"
				+ "}"		
			+ "]"
		+ "}";
			
	
	
	public @Test void testServiceIdentification () {
		final ServiceIdentification si = JsonFactory.serviceIdentification ("{ \"serviceType\":\"WMS\", \"serviceEndpoint\":\"http://serviceendpoint.com\", \"serviceVersion\":\"1.0.0\" }");
		
		assertEquals ("WMS", si.getServiceType ());
		assertEquals ("http://serviceendpoint.com", si.getServiceEndpoint ());
		assertEquals ("1.0.0", si.getServiceVersion ());
	}
	
	@Test (expected = RuntimeException.class)
	public void testServiceIdentificationMissingValue () {
		JsonFactory.serviceIdentification ("{ \"serviceType\":\"WMS\", \"serviceEndpoint\":\"http://serviceendpoint.com\" }");
	}

	public @Test void testService () {
		final Service s = JsonFactory.service ("{ \"id\": \"service-1\", \"label\": \"Service 1\", \"identification\": { \"serviceType\":\"WMS\", \"serviceEndpoint\":\"http://serviceendpoint.com\", \"serviceVersion\":\"1.0.0\" } }");
		
		assertEquals ("service-1", s.getId ());
		assertEquals ("Service 1", s.getLabel ());
		assertEquals ("WMS", s.getIdentification ().getServiceType ());
		assertEquals ("http://serviceendpoint.com", s.getIdentification ().getServiceEndpoint ());
		assertEquals ("1.0.0", s.getIdentification ().getServiceVersion ());
	}
	
	@Test (expected = RuntimeException.class)
	public void testServiceMissingIdentification () {
		JsonFactory.service ("{ \"id\": \"service-1\", \"label\": \"Service 1\" }");
	}
	
	@Test
	public void testQName () {
		final QName q = JsonFactory.qName ("{ \"localName\": \"name\", \"namespace\": \"namespace\" }");
		
		assertEquals ("name", q.getLocalName ());
		assertEquals ("namespace", q.getNamespace ());
	}
	
	@Test
	public void testQNameLocalOnly () {
		final QName q = JsonFactory.qName ("{ \"localName\": \"name\" }");
		
		assertEquals ("name", q.getLocalName ());
		assertNull (q.getNamespace ());
	}
	
	@Test
	public void testQNameNameOnly () {
		final QName q = JsonFactory.qName ("\"name\"");
		
		assertEquals ("name", q.getLocalName ());
		assertNull (q.getNamespace ());
	}
	
	@Test (expected = RuntimeException.class)
	public void testQNameMissingLocalName () {
		JsonFactory.qName ("{ \"namespace\": \"namespace\" }");
	}
	
	private void assertMapDefinition (final MapDefinition def) {
		assertEquals ("mapdef-1", def.getId ());
		assertEquals ("Map definition 1", def.getLabel ());
		
		final List<Layer> layers = def.getRootLayers ();
		
		assertEquals (1, layers.size ());
		
		
		assertEquals ("map-layer-1", layers.get (0).getId ());
		assertEquals ("Layer 1", layers.get (0).getLabel ());
		assertEquals ("service-layer-1", layers.get (0).getLayers().get (0).getServiceLayers ().get (0).getId ());
		assertEquals ("Service layer 1", layers.get (0).getLayers().get (0).getServiceLayers ().get (0).getLabel ());
		assertEquals ("layername", layers.get (0).getLayers().get (0).getServiceLayers ().get (0).getName().getLocalName ());
		assertEquals ("service-1", layers.get (0).getLayers().get (0).getServiceLayers ().get (0).getService ().getId ());
		assertEquals ("Service 1", layers.get (0).getLayers().get (0).getServiceLayers ().get (0).getService ().getLabel ());
		
		assertEquals ("map-layer-2", layers.get (0).getLayers ().get (0).getId ());
		assertEquals ("Layer 2", layers.get (0).getLayers ().get (0).getLabel ());
		assertEquals ("service-layer-2", layers.get (0).getLayers ().get (1).getServiceLayers ().get (0).getId ());
		
		assertNull (layers.get (0).getLayers().get (0).getServiceLayers ().get (0).getFeatureType ());
		assertNotNull (layers.get (0).getLayers ().get (1).getServiceLayers ().get (0).getFeatureType ());
		assertEquals ("feature-type-1", layers.get (0).getLayers ().get (1).getServiceLayers ().get (0).getFeatureType ().getId ());
		assertEquals ("service-3", layers.get (0).getLayers ().get (1).getServiceLayers ().get (0).getFeatureType ().getService ().getId ());
		assertEquals ("featureTypeName", layers.get (0).getLayers ().get (1).getServiceLayers ().get (0).getFeatureType ().getName ().getLocalName ());

		
		assertTrue (def.getLayers ().containsKey ("map-layer-1"));
		assertTrue (def.getLayers ().containsKey ("map-layer-2"));
		assertTrue (def.getServiceLayers ().containsKey ("service-layer-1"));
		assertTrue (def.getServiceLayers ().containsKey ("service-layer-2"));
		assertTrue (def.getServices ().containsKey ("service-1"));
		assertTrue (def.getServices ().containsKey ("service-2"));
		assertTrue (def.getServices ().containsKey ("service-3"));
		assertTrue (def.getFeatureTypes ().containsKey ("feature-type-1"));
	}
}
