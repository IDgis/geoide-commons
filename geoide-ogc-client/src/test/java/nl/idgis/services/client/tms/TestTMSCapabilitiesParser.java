package nl.idgis.services.client.tms;

import static org.junit.Assert.*;

import java.io.InputStream;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.services.tms.TMSCapabilities;

import org.junit.Test;

public class TestTMSCapabilitiesParser {

	@Test
	public void testTileMapService100 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/services/client/tms/tilemapservice-1.0.0.xml");

		final TMSCapabilities.TileMapService capabilities = TMSCapabilitiesParser.parseTileMapServiceCapabilities (is);
		
		is.close ();
		
		assertNotNull (capabilities);
		
		assertEquals ("1.0.0", capabilities.version ());
		assertEquals ("Tile Map Service", capabilities.serviceIdentification ().title ());
		assertEquals ("A Tile Map Service served by GeoWebCache", capabilities.serviceIdentification ().abstractText ());
		
		// Test tilemaps:
		assertEquals (30, capabilities.tileMaps ().size ());
		assertEquals ("brtachtergrondkaart", capabilities.tileMaps ().get (0).title ());
		assertEquals ("EPSG:25831", capabilities.tileMaps ().get (0).srs ());
		assertEquals ("local", capabilities.tileMaps ().get (0).profile ());
		assertEquals ("http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0/brtachtergrondkaart@EPSG%3A25831%3ARWS@png", capabilities.tileMaps ().get (0).href ());
		
		// Test layers:
		assertEquals (27, capabilities.layers ().size ());
		final TMSCapabilities.Layer firstLayer = capabilities.layers ().iterator ().next ();
		assertEquals ("brtachtergrondkaart", firstLayer.name ());
		assertEquals ("brtachtergrondkaart", firstLayer.title ());
		assertEquals (4, firstLayer.tileMaps ().size ());
		assertEquals (2, firstLayer.crss ().size ());
		assertTrue (firstLayer.supportsCRS ("epsg:25831"));
		assertTrue (firstLayer.supportsCRS ("epsg:28992"));
	}
	
	@Test
	public void testTileMap100 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/services/client/tms/tilemap-1.0.0.xml");

		final TMSCapabilities.TileMapLayer capabilities = TMSCapabilitiesParser.parseTileMapCapabilities ("http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0/brtachtergrondkaart@EPSG%3A25831%3ARWS@png8", is);
		
		is.close ();
		
		assertNotNull (capabilities);
		
		assertEquals ("brtachtergrondkaart", capabilities.title ());
		assertEquals ("EPSG:25831", capabilities.srs ());
		assertEquals (-2404683.40738879, capabilities.boundingBox().minX (), 0.001);
		assertEquals (3997657.58466454, capabilities.boundingBox().minY (), 0.001);
		assertEquals (4046516.592611209, capabilities.boundingBox().maxX (), 0.001);
		assertEquals (8298457.5846645385, capabilities.boundingBox().maxY (), 0.001);
		assertEquals (-2404683.40738879, capabilities.origin ().x (), 0.001);
		assertEquals (3997657.58466454, capabilities.origin ().y (), 0.001);
		assertEquals (256, capabilities.tileFormat ().width ());
		assertEquals (256, capabilities.tileFormat ().height ());
		assertEquals (new MimeContentType ("image/png"), capabilities.tileFormat ().mimeType ());
		assertEquals ("png8", capabilities.tileFormat ().extension ());
		assertEquals ("local", capabilities.profile ());
		assertEquals (12, capabilities.tileSets ().size ());
		assertEquals ("http://geodata.nationaalgeoregister.nl/tiles/service/tms/1.0.0/brtachtergrondkaart@EPSG%3A25831%3ARWS@png8/0", capabilities.tileSets ().get (0).href ());
		assertEquals (2799.9999999999995, capabilities.tileSets ().get (0).unitsPerPixel (), 0.001);
		assertEquals (0, capabilities.tileSets ().get (0).order ());
	}
	
	@Test
	public void testGenericCapabilitiesTileMapService100 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/services/client/tms/tilemapservice-1.0.0.xml");

		final TMSCapabilities.TileMapService capabilities = TMSCapabilitiesParser.parseCapabilities ("http://geodata.nationaalgeoregister.nl/tms/1.0.0/", is);
		
		is.close ();
		
		assertNotNull (capabilities);
		
		assertEquals ("1.0.0", capabilities.version ());
		assertEquals ("Tile Map Service", capabilities.serviceIdentification ().title ());
		assertEquals ("A Tile Map Service served by GeoWebCache", capabilities.serviceIdentification ().abstractText ());
	}
	
	@Test
	public void testGenericCapabilitiesTileMap100 () throws Throwable {
		final InputStream is = getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/services/client/tms/tilemap-1.0.0.xml");

		final TMSCapabilities.TileMapService capabilities = TMSCapabilitiesParser.parseCapabilities ("http://geodata.nationaalgeoregister.nl/tms/1.0.0/", is);
		
		is.close ();
		
		assertNotNull (capabilities);
		
		assertEquals ("1.0.0", capabilities.version ());
		assertEquals ("brtachtergrondkaart", capabilities.serviceIdentification ().title ());
		assertEquals ("", capabilities.serviceIdentification ().abstractText ());
		
		assertEquals (1, capabilities.tileMaps ().size ());
		assertEquals ("brtachtergrondkaart", capabilities.tileMaps ().get (0).title ());
		assertEquals ("EPSG:25831", capabilities.tileMaps ().get (0).srs ());
	}
}
