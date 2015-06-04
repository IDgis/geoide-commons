package nl.idgis.services.client.arcgis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import nl.idgis.services.arcgis.ArcGISRestCapabilities;

import org.junit.Test;

public class TestArcGISRestCapabilitiesParser {

	@Test
	public void testAcrGISRest10_1 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/services/client/arcgis/arcgisrest-10.1.json");

		final ArcGISRestCapabilities capabilities = ArcGISRestCapabilitiesParser.parseCapabilities (is);
		
		is.close ();
		
		assertNotNull (capabilities);
		
		assertEquals ("10.11", capabilities.version ());
		assertEquals ("ondergrond", capabilities.serviceIdentification ().title ());
		
		assertEquals (26, capabilities.allLayers ().size ());
		assertEquals (1, capabilities.layers ().size ());
		
		assertEquals ("0", capabilities.layers ().get (0).name ());
		assertEquals ("Ondergrond in kleur", capabilities.layers ().get (0).title ());
		assertEquals (6, capabilities.layers ().get (0).layers ().size ());
		
		assertEquals ("1", capabilities.layers ().get (0).layers ().get (0).name ());
		assertEquals ("Administratieve grenzen", capabilities.layers ().get (0).layers ().get (0).title ());
		
		assertEquals ("2", capabilities.layers ().get (0).layers ().get (0).layers ().get (0).name ());
		assertEquals ("Provinciegrens Gelderland", capabilities.layers ().get (0).layers ().get (0).layers ().get (0).title ());
		
		assertEquals ("6", capabilities.layers ().get (0).layers ().get (1).name ());
		assertEquals ("Topografie 1:10.000", capabilities.layers ().get (0).layers ().get (1).title ());
	}

}
