package nl.idgis.services.client.tms;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.service.Capabilities.BoundingBox;
import nl.idgis.geoide.commons.domain.service.Capabilities.Point;
import nl.idgis.services.tms.TMSCapabilities;
import nl.idgis.services.tms.TMSCapabilities.TileFormat;
import nl.idgis.services.tms.TMSCapabilities.TileMap;
import nl.idgis.services.tms.TMSCapabilities.TileMapLayer;
import nl.idgis.services.tms.TMSCapabilities.TileSet;

public class TMSCapabilitiesParser {

	public static TMSCapabilities.TileMapService parseCapabilities (final String href, final InputStream inputStream) throws ParseException {
		if (href == null) {
			throw new NullPointerException ("href cannot be null");
		}
		
		try {
			final XMLStreamReader reader = XMLInputFactory.newFactory ().createXMLStreamReader (inputStream);
			
			// Skip to the root tag:
			reader.nextTag ();
			
			if ("TileMapService".equals (reader.getLocalName ())) {
				return parseCapabilities (reader);
			} else if ("TileMap".equals (reader.getLocalName ())) {
				final TMSCapabilities.TileMapLayer layer = parseTileMapCapabilities (href, reader);
				
				return new TMSCapabilities.TileMapService (
						layer.version (), 
						layer.title (), 
						layer.abstractText (), 
						Arrays.asList (new TMSCapabilities.TileMap[] { layer })
					);
			} else {
				throw new ParseException (String.format ("Not a TileMapService or TileMap capabilities document (root tag: %s)", reader.getLocalName ())); 
			}
		} catch (XMLStreamException e) {
			throw new ParseException ("Error parsing XML stream", e);
		} catch (FactoryConfigurationError e) {
			throw new ParseException ("Error parsing XML stream", e);
		}
	}
	
	public static TMSCapabilities.TileMapService parseTileMapServiceCapabilities (final InputStream inputStream) throws ParseException {
		try {
			final XMLStreamReader reader = XMLInputFactory.newFactory ().createXMLStreamReader (inputStream);
			
			// Skip to the root tag:
			reader.nextTag ();
			
			if (!"TileMapService".equals (reader.getLocalName ())) {
				throw new ParseException (String.format ("Not a TileMapService capabilities document (root tag: %s)", reader.getLocalName ())); 
			}
	
			// Parse the capabilities:
			return parseCapabilities (reader);
		} catch (XMLStreamException e) {
			throw new ParseException ("Error parsing XML stream", e);
		} catch (FactoryConfigurationError e) {
			throw new ParseException ("Error parsing XML stream", e);
		}
	}
	
	public static TMSCapabilities.TileMapLayer parseTileMapCapabilities (final String href, final InputStream inputStream) throws ParseException {
		try {
			final XMLStreamReader reader = XMLInputFactory.newFactory ().createXMLStreamReader (inputStream);
			
			// Skip to the root tag:
			reader.nextTag ();
			
			if (!"TileMap".equals (reader.getLocalName ())) {
				throw new ParseException (String.format ("Not a TileMap capabilities document (root tag: %s)", reader.getLocalName ())); 
			}
			
			// Parse the capabilities:
			return parseTileMapCapabilities (href, reader);
		} catch (XMLStreamException e) {
			throw new ParseException ("Error parsing XML stream", e);
		} catch (FactoryConfigurationError e) {
			throw new ParseException ("Error parsing XML stream", e);
		}
	}
	
	private static TMSCapabilities.TileMapService parseCapabilities (final XMLStreamReader reader) throws ParseException, XMLStreamException {
		final String version = reader.getAttributeValue (null, "version");
		
		if (version == null) {
			throw new ParseException ("TMS capabilities document does not specify a version");
		}
		
		String title = null;
		String abstractText = null;
		List<TileMap> tileMaps = null;
		
		while (reader.hasNext ()) {
			// Skip to the next tag:
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}

			if (is (reader, "Title")) {
				title = reader.getElementText ();
			} else if (is (reader, "Abstract")) {
				abstractText = reader.getElementText ();
			} else if (is (reader, "TileMaps")) {
				tileMaps = parseTileMaps (reader);
			}
		}
		
		if (title == null) {
			throw new ParseException ("TileMapService must provide a title");
		}
		if (tileMaps == null || tileMaps.isEmpty ()) {
			throw new ParseException ("TileMapService must provide at least one TileMap");
		}
		
		return new TMSCapabilities.TileMapService (version, title, abstractText, tileMaps);
	}
	
	private static List<TileMap> parseTileMaps (final XMLStreamReader reader) throws XMLStreamException, ParseException {
		final List<TileMap> tileMaps = new ArrayList<> ();
		
		while (!isEnd (reader, "TileMaps")) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}

			if (is (reader, "TileMap")) {
				tileMaps.add (parseTileMap (reader));
			}
		}
		
		return tileMaps;
	}
	
	private static TileMap parseTileMap (final XMLStreamReader reader) throws XMLStreamException, ParseException {
		final String title = reader.getAttributeValue (null, "title");
		final String srs = reader.getAttributeValue (null, "srs");
		final String profile = reader.getAttributeValue (null, "profile");
		final String href = reader.getAttributeValue (null, "href");
		
		if (title == null) {
			throw new ParseException ("A title must be provided for each TileMap");
		}
		if (srs == null) {
			throw new ParseException ("A SRS must be provided for each TileMap");
		}
		if (profile == null) {
			throw new ParseException ("A profile must be provided for each TileMap");
		}
		if (href == null) {
			throw new ParseException ("A href must be provided for each TileMap");
		}
		
		return new TileMap (title, srs, profile, href);
	}
	
	private static TileMapLayer parseTileMapCapabilities (final String href, final XMLStreamReader reader) throws XMLStreamException, ParseException {
		String title = null;
		String srs = null;
		String profile = null;
		
		final String version = reader.getAttributeValue (null, "version");
		String abstractText = null;
		BoundingBox boundingBox = null;
		Point origin = null;
		TileFormat tileFormat = null;
		List<TileSet> tileSets = null;
		
		while (!isEnd (reader, "TileMap")) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}

			if (is (reader, "Title")) {
				title = reader.getElementText ().trim ();
			} else if (is (reader, "Abstract")) {
				abstractText = reader.getElementText ().trim ();
			} else if (is (reader, "SRS")) {
				srs = reader.getElementText ().trim ();
			} else if (is (reader, "BoundingBox")) {
				boundingBox = parseBoundingBox (reader);
			} else if (is (reader, "Origin")) {
				origin = parseOrigin (reader);
			} else if (is (reader, "TileFormat")) {
				tileFormat = parseTileFormat (reader);
			} else if (is (reader, "TileSets")) {
				profile = reader.getAttributeValue (null, "profile");
				tileSets = parseTileSets (reader);
			}
		}
		
		if (version == null) {
			throw new ParseException ("A TileMap should have a version");
		}
		if (title == null || title.isEmpty ()) {
			throw new ParseException ("A TileMap should have a title");
		}
		if (srs == null || srs.isEmpty ()) {
			throw new ParseException ("A TileMap should have a SRS code");
		}
		if (profile == null || profile.isEmpty ()) {
			throw new ParseException ("A TileMap should have a profile code");
		}
		if (boundingBox == null) {
			throw new ParseException ("A TileMap should have a BoundingBox");
		}
		if (origin == null) {
			throw new ParseException ("A TileMap should have an Origin");
		}
		if (tileFormat == null) {
			throw new ParseException ("A TileMap should have a TileFormat");
		}
		if (tileSets == null || tileSets.isEmpty ()) {
			throw new ParseException ("A TileMap should have at least one TileSet");
		}
		
		return new TileMapLayer (
				new TileMap (
					title, 
					srs, 
					profile, 
					href
				),
				version,
				abstractText, 
				boundingBox, 
				origin, 
				tileFormat, 
				tileSets
			);
	}
	
	private static BoundingBox parseBoundingBox (final XMLStreamReader reader) throws ParseException {
		final String minx = reader.getAttributeValue (null, "minx");
		final String miny = reader.getAttributeValue (null, "miny");
		final String maxx = reader.getAttributeValue (null, "maxx");
		final String maxy = reader.getAttributeValue (null, "maxy");
		
		if (minx == null) {
			throw new ParseException ("BoundingBox does not specify a minx attribute");
		}
		if (miny == null) {
			throw new ParseException ("BoundingBox does not specify a miny attribute");
		}
		if (maxx == null) {
			throw new ParseException ("BoundingBox does not specify a maxx attribute");
		}
		if (maxy == null) {
			throw new ParseException ("BoundingBox does not specify a maxy attribute");
		}
		
		try {
			return new BoundingBox (
				Double.parseDouble (minx), 
				Double.parseDouble (miny), 
				Double.parseDouble (maxx), 
				Double.parseDouble (maxy)
			);
		} catch (NumberFormatException e) {
			throw new ParseException ("Illegal value in BoundingBox", e);
		}
	}
	
	private static Point parseOrigin (final XMLStreamReader reader) throws ParseException {
		final String x = reader.getAttributeValue (null, "x");
		final String y = reader.getAttributeValue (null, "y");
		
		if (x == null) {
			throw new ParseException ("Origin does not specify an x attribute");
		}
		if (y == null) {
			throw new ParseException ("Origin does not specify an y attribute");
		}
		
		try {
			return new Point (Double.parseDouble (x), Double.parseDouble (y));
		} catch (NumberFormatException e) {
			throw new ParseException ("Invalid number format in Origin");
		}
	}
	
	private static TileFormat parseTileFormat (final XMLStreamReader reader) throws ParseException {
		final String width = reader.getAttributeValue (null, "width");
		final String height = reader.getAttributeValue (null, "height");
		final String mimeType = reader.getAttributeValue (null, "mime-type");
		final String extension = reader.getAttributeValue (null, "extension");
		
		if (width == null) {
			throw new ParseException ("TileFormat does not specify a width attribute");
		}
		if (height == null) {
			throw new ParseException ("TileFormat does not specify a height attribute");
		}
		if (mimeType == null) {
			throw new ParseException ("TileFormat does not specify a mimeType attribute");
		}
		if (!MimeContentType.isValid (mimeType)) {
			throw new ParseException (String.format ("TileFormat specifies an invalid mimeType: %s", mimeType));
		}
		if (extension == null) {
			throw new ParseException ("TileFormat does not specify an extension attribute");
		}
		
		try {
			return new TileFormat (
					Integer.parseInt (width), 
					Integer.parseInt (height), 
					new MimeContentType (mimeType), 
					extension
				);
		} catch (NumberFormatException e) {
			throw new ParseException ("TileFormat specifies an invalid width and/or height");
		}
	}
	
	private static List<TileSet> parseTileSets (final XMLStreamReader reader) throws XMLStreamException, ParseException {
		
		final List<TileSet> tileSets = new ArrayList<> ();
		
		while (!isEnd (reader, "TileMap")) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}
			
			if (is (reader, "TileSet")) {
				tileSets.add (parseTileSet (reader));
			}
		}	
		
		if (tileSets.isEmpty ()) {
			throw new ParseException ("At least one TileSet must be specified");
		}
		
		return Collections.unmodifiableList (tileSets);
	}
	
	private static TileSet parseTileSet (final XMLStreamReader reader) throws ParseException {
		final String href = reader.getAttributeValue (null, "href");
		final String unitsPerPixel = reader.getAttributeValue (null, "units-per-pixel");
		final String order = reader.getAttributeValue (null, "order");
		
		if (href == null) {
			throw new ParseException ("TileSet does not specify a href attribute");
		}
		if (unitsPerPixel == null) {
			throw new ParseException ("TileSet does not specify a units-per-pixel attribute");
		}
		if (order == null) {
			throw new ParseException ("TileSet does not specify a order attribute");
		}
		
		try {
			return new TileSet (
					href, 
					Double.parseDouble (unitsPerPixel), 
					Integer.parseInt (order)
				);
		} catch (NumberFormatException e) {
			throw new ParseException ("Invalid number format in TileSet");
		}
	}
	
	
	private static boolean is (final XMLStreamReader reader, final String localName) {
		return reader.isStartElement () && localName.equals (reader.getLocalName ());
	}
	
	private static boolean isEnd (final XMLStreamReader reader, final String localName) throws XMLStreamException {
		return !reader.hasNext () || (reader.isEndElement () && localName.equals (reader.getLocalName ()));
	}
	
		
	public static class ParseException extends Exception {
		private static final long serialVersionUID = -605117083683271588L;

		public ParseException (final String message) {
			super (message);
		}
		
		public ParseException (final String message, final Throwable cause) {
			super (message, cause);
		}
	}
}
