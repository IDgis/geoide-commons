package nl.idgis.geoide.map.provider;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.idgis.geoide.commons.domain.MapDefinition;

public class JsonMapProviderBuilderTest {

	private final ObjectMapper mapper = new ObjectMapper ();
	private JsonMapProviderBuilder builder;
	
	@Before
	public void createBuilder () {
		builder = JsonMapProviderBuilder.create ();
	}
	
	@Test
	public void testLoadMap () throws Throwable {
		final MapDefinition map = builder
			.addJson (loadResource ("test-map.json"))
			.build ()
			.getMapDefinition ("test-map");
		
		assertNotNull (map);
		assertEquals ("test-map", map.getId ());
		assertEquals ("Test map", map.getLabel ());
		
		assertEquals (2, map.getRootLayers ().size ());
		assertEquals ("test-layer-1", map.getRootLayers ().get (0).getLayer ().getId ());
		assertEquals ("Layer 1", map.getRootLayers ().get (0).getLayer ().getLabel ());
		assertEquals ("default", map.getRootLayers ().get (0).getLayer ().getLayerType ());
		assertEquals ("test-layer-2", map.getRootLayers ().get (1).getLayer ().getId ());
		assertEquals ("Layer 2", map.getRootLayers ().get (1).getLayer ().getLabel ());
		assertEquals ("default", map.getRootLayers ().get (1).getLayer ().getLayerType ());
		
		assertEquals (1, map.getRootLayers ().get (0).getLayer ().getServiceLayers ().size ());
		assertEquals ("test-servicelayer-1", map.getRootLayers ().get (0).getLayer ().getServiceLayers ().get (0).getId ());
		assertEquals ("Test servicelayer 1", map.getRootLayers ().get (0).getLayer ().getServiceLayers ().get (0).getLabel ());
		assertEquals ("test-service-1", map.getRootLayers ().get (0).getLayer ().getServiceLayers ().get (0).getService ().getId ());
		assertEquals ("Test service 1", map.getRootLayers ().get (0).getLayer ().getServiceLayers ().get (0).getService ().getLabel ());
		
		assertEquals (1, map.getRootLayers ().get (1).getLayer ().getServiceLayers ().size ());
		assertEquals ("test-servicelayer-2", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getId ());
		assertEquals ("Test servicelayer 2", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getLabel ());
		assertEquals ("test-service-2", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getService ().getId ());
		assertEquals ("Test service 2", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getService ().getLabel ());
		
		assertEquals ("test-feature-type-1", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getFeatureType ().getId ());
		assertEquals ("Test feature type 1", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getFeatureType ().getLabel ());
		assertEquals ("test-service-2", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getFeatureType ().getService ().getId ());
	}
	
	@Test
	public void testOverrideServiceLayer () throws Throwable {
		final MapDefinition map = builder
				.addJson (loadResource ("test-map.json"))
				.addJson (loadResource ("override-service-layer.json"))
				.build ()
				.getMapDefinition ("test-map");
		
		assertEquals (1, map.getRootLayers ().get (1).getLayer ().getServiceLayers ().size ());
		assertEquals ("test-servicelayer-2", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getId ());
		assertEquals ("Test servicelayer 2 override", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getLabel ());
		assertEquals ("test-service-2", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getService ().getId ());
		assertEquals ("Test service 2", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getService ().getLabel ());
	}
	
	@Test
	public void testServiceOverride () throws Throwable {
		final MapDefinition map = builder
				.addJson (loadResource ("test-map.json"))
				.addJson (loadResource ("override-service.json"))
				.build ()
				.getMapDefinition ("test-map");
		
		assertEquals (1, map.getRootLayers ().get (1).getLayer ().getServiceLayers ().size ());
		assertEquals ("test-service-1", map.getRootLayers ().get (0).getLayer ().getServiceLayers ().get (0).getService ().getId ());
		assertEquals ("Test service 1 override", map.getRootLayers ().get (0).getLayer ().getServiceLayers ().get (0).getService ().getLabel ());
	}
	
	@Test
	public void testLayerOverride () throws Throwable {
		final MapDefinition map = builder
				.addJson (loadResource ("test-map.json"))
				.addJson (loadResource ("override-layer.json"))
				.build ()
				.getMapDefinition ("test-map");
		
		assertEquals (2, map.getRootLayers ().size ());
		assertEquals ("test-layer-1", map.getRootLayers ().get (0).getLayer ().getId ());
		assertEquals ("Layer 1", map.getRootLayers ().get (0).getLayer ().getLabel ());
		assertEquals ("default-override", map.getRootLayers ().get (0).getLayer ().getLayerType ());
		assertEquals ("test-servicelayer-2", map.getRootLayers ().get (0).getLayer ().getServiceLayers ().get (0).getId ());
	}
	
	@Test
	public void testMapOverride () throws Throwable {
		final MapDefinition map = builder
				.addJson (loadResource ("test-map.json"))
				.addJson (loadResource ("override-map.json"))
				.build ()
				.getMapDefinition ("test-map");
		
		assertNotNull (map);
		assertEquals ("test-map", map.getId ());
		assertEquals ("Test map override", map.getLabel ());
		
		assertEquals (0, map.getRootLayers ().size ());
	}
	
	@Test
	public void testFeatureTypeOverride () throws Throwable {
		final MapDefinition map = builder
				.addJson (loadResource ("test-map.json"))
				.addJson (loadResource ("override-feature-type.json"))
				.build ()
				.getMapDefinition ("test-map");
		
		assertEquals ("test-feature-type-1", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getFeatureType ().getId ());
		assertEquals ("Test feature type 1 override", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getFeatureType ().getLabel ());
		assertEquals ("test-service-1", map.getRootLayers ().get (1).getLayer ().getServiceLayers ().get (0).getFeatureType ().getService ().getId ());
	}

	private JsonNode loadResource (final String resource) throws JsonProcessingException, IOException {
		final InputStream inputStream = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/geoide/map/provider/" + resource);
		assertNotNull (inputStream);
		return mapper.readTree (inputStream);
	}
}
