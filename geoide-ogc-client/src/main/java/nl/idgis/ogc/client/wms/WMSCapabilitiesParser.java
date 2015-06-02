package nl.idgis.ogc.client.wms;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.ogc.wms.WMSCapabilities;
import nl.idgis.ogc.wms.WMSCapabilities.Layer;
import nl.idgis.ogc.wms.WMSCapabilities.Request;
import nl.idgis.ogc.wms.WMSCapabilities.RequestType;
import nl.idgis.ogc.wms.WMSCapabilities.Service;
import nl.idgis.ogc.wms.WMSCapabilities.Style;
import nl.idgis.services.Capabilities.BoundingBox;

public class WMSCapabilitiesParser {

	public static WMSCapabilities parseCapabilities (final InputStream inputStream) throws ParseException {
		try {
			final XMLInputFactory factory = XMLInputFactory.newFactory ();
			
			// Be more lenient towards "semi"-correct capabilities documents: disable DTD support.
			factory.setProperty (XMLInputFactory.SUPPORT_DTD, false);
			
			final XMLStreamReader reader = factory.createXMLStreamReader (inputStream);
			
			// Skip to the root tag, also skip DTD's:
			while (!reader.isStartElement ()) {
				reader.next ();
			}
			
			if (!"WMS_Capabilities".equals (reader.getLocalName ()) && !"WMT_MS_Capabilities".equals (reader.getLocalName ())) {
				throw new ParseException (String.format ("Not a WMS capabilities document (root tag: %s)", reader.getLocalName ())); 
			}

			return parseCapabilities (reader);
		} catch (XMLStreamException e) {
			throw new ParseException ("Error parsing XML stream", e);
		} catch (FactoryConfigurationError e) {
			throw new ParseException ("Error parsing XML stream", e);
		}
	}
	
	private static WMSCapabilities parseCapabilities (final XMLStreamReader reader) throws ParseException, XMLStreamException {
		final String version = getAttributeValue (reader, "version");
		
		if (version == null) {
			throw new ParseException ("WMS capabilities document must specify a version");
		}
		
		Service service = null;
		List<Request> requests = null;
		Set<String> exceptions = null;
		final List<Layer> layers = new ArrayList<> ();
		
		while (reader.hasNext ()) {
			// Skip to the next tag:
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}
			
			if ("Service".equals (reader.getLocalName ())) {
				service = parseService (reader);
			} else if ("Request".equals (reader.getLocalName ())) {
				requests = parseRequests (reader);
			} else if ("Exception".equals (reader.getLocalName ())) {
				exceptions = parseExceptions (reader);
			} else if ("Layer".equals (reader.getLocalName ())) {
				layers.add (parseLayer (reader));
			}
		}
		
		if (service == null) {
			throw new ParseException ("Capabilities document doesn't specify service identification");
		}
		if (requests == null || requests.isEmpty ()) {
			throw new ParseException ("Capabilities document doesn't specify any request capabilities");
		}
		if (layers.isEmpty ()) {
			throw new ParseException ("Capabilities document doesn't specify any layers");
		}
		
		return new WMSCapabilities (version, service, requests, exceptions, layers);
	}
	
	private static Service parseService (final XMLStreamReader reader) throws XMLStreamException, ParseException {
		String name = null;
		String title = null;
		String abstractText = null;
		final Set<String> keywords = new HashSet<> ();
		
		while (!isEnd (reader, "Service")) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}
			
			if (is (reader, "Name")) {
				name = reader.getElementText ();
			} else if (is (reader, "Title")) {
				title = reader.getElementText ();
			} else if (is (reader, "Abstract")) {
				abstractText = reader.getElementText ();
			} else if (is (reader, "Keyword")) {
				keywords.add (reader.getElementText ());
			}
		}
		
		if (name == null) {
			throw new ParseException ("No service name specified");
		}
		
		return new Service (name, title, abstractText, keywords);
	}
	
	private static List<Request> parseRequests (final XMLStreamReader reader) throws XMLStreamException, ParseException {
		final List<Request> requests = new ArrayList<> ();
		
		while (!isEnd (reader, "Request")) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}

			if (is (reader, "GetCapabilities")) {
				requests.add (parseRequest (reader, RequestType.GET_CAPABILITIES, "GetCapabilities"));
			} else if (is (reader, "GetMap")) {
				requests.add (parseRequest (reader, RequestType.GET_MAP, "GetMap"));
			} else if (is (reader, "GetFeatureInfo")) {
				requests.add (parseRequest (reader, RequestType.GET_FEATURE_INFO, "GetFeatureInfo"));
			} else if (is (reader, "GetLegendGraphic")) {
				requests.add (parseRequest (reader, RequestType.GET_LEGEND_GRAPHIC, "GetLegendGraphic"));
			} else if (is (reader, "DescribeLayer")) {
				requests.add (parseRequest (reader, RequestType.DESCRIBE_LAYER, "DescribeLayer"));
			}
		}
		
		return requests;
	}
	
	private static Request parseRequest (final XMLStreamReader reader, final RequestType type, final String tagName) throws XMLStreamException, ParseException {
		final Set<MimeContentType> formats = new HashSet<> ();
		String httpGet = null;
		String httpPost = null;
		
		while (!isEnd (reader, tagName)) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}

			if (is (reader, "Format")) {
				final String format = reader.getElementText ();
				if (!MimeContentType.isValid (format)) {
					//Do not throw error but just skip
					
					//throw new ParseException (String.format ("Request %s has an illegal format %s", tagName, format));
				} else {
					formats.add (new MimeContentType (format));
				}	
			} else if (is (reader, "Get")) {
				httpGet = parseOnlineResource (reader, "Get");
			} else if (is (reader, "Post")) {
				httpPost = parseOnlineResource (reader, "Post");
			}
		}
		
		if (formats.isEmpty ()) {
			throw new ParseException (String.format ("Request %s didn't specify any formats", tagName));
		}
		if (httpGet == null && httpPost == null) {
			throw new ParseException (String.format ("Request %s didn't specify HTTP GET nor HTTP POST", tagName));
		}
		
		return new Request (type, formats, httpGet, httpPost);
	}
	
	private static String parseOnlineResource (final XMLStreamReader reader, final String tagName) throws XMLStreamException, ParseException {
		String href = null;
		
		while (!isEnd (reader, tagName)) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}
		
			if (is (reader, "OnlineResource")) {
				href = getAttributeValue (reader, "href");
			}
		}
		
		if (href == null) {
			throw new ParseException (String.format ("Online resource for %s is missing or doesn't provide a href property", tagName));
		}
		
		return href;
	}
	
	private static String getAttributeValue (final XMLStreamReader reader, final String name) {
		for (int i = 0; i < reader.getAttributeCount (); ++ i) {
			final String localName = reader.getAttributeLocalName (i);
			if (localName.equals (name)) {
				return reader.getAttributeValue (i);
			}
		}
		return null;
	}
	
	private static Set<String> parseExceptions (final XMLStreamReader reader) throws XMLStreamException, ParseException {
		final Set<String> exceptions = new HashSet<> ();
				
		while (!isEnd (reader, "Exception")) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}
			
			if (is (reader, "Format")) {
				exceptions.add (reader.getElementText ());
			}
		}
		
		return exceptions;
	}
	
	private static Layer parseLayer (final XMLStreamReader reader) throws XMLStreamException, ParseException {
		final String queryableValue = getAttributeValue (reader, "queryable");
		final boolean queryable = queryableValue != null && ("1".equals (queryableValue.trim ()) || "true".equals (queryableValue.trim ().toLowerCase ()));
		
		String name = null;
		String title = null;
		String abstractText = null;
		final Set<String> crss = new HashSet<> ();
		final Map<String, BoundingBox> boundingBoxes = new HashMap<> ();
		final List<Style> styles = new ArrayList<> ();
		final List<Layer> layers = new ArrayList<WMSCapabilities.Layer> ();
		
		boolean skipNext = false;
		
		while (!isEnd (reader, "Layer")) {
			if (!skipNext) {
				reader.next ();
			}
			skipNext = false;
			if (!reader.isStartElement ()) {
				continue;
			}

			if (is (reader, "Name")) {
				name = reader.getElementText ();
			} else if (is (reader, "Title")) {
				title = reader.getElementText ();
			} else if (is (reader, "Abstract")) {
				abstractText = reader.getElementText ();
			} else if (is (reader, "BoundingBox")) {
				parseBoundingBox (reader, boundingBoxes);
			} else if (is (reader, "SRS") || is (reader, "CRS")) {
				crss.add (reader.getElementText ());
			} else if (is (reader, "Style")) {
				styles.add (parseStyle (reader));
			} else if (is (reader, "Layer")) {
				layers.add (parseLayer (reader));
				reader.next ();
				skipNext = true;
			}
		}
		
		return new Layer (queryable, name, title, abstractText, crss, boundingBoxes, styles, layers);
	}
	
	private static Style parseStyle (final XMLStreamReader reader) throws XMLStreamException, ParseException {
		String name = null;
		String title = null;
		
		while (!isEnd (reader, "Style")) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}
			
			if (is (reader, "Name")) {
				name = reader.getElementText ();
			} else if (is (reader, "Title")) {
				title = reader.getElementText ();
			}
		}
		
		if (name == null) {
			throw new ParseException ("Style must have a name");
		}
		
		return new Style (name, title);
	}
	
	private static void parseBoundingBox (final XMLStreamReader reader, final Map<String, BoundingBox> boundingBoxes) throws ParseException {
		final String crs;
		
		if (getAttributeValue (reader, "CRS") != null) {
			crs = getAttributeValue (reader, "CRS");
		} else {
			crs = getAttributeValue (reader, "SRS");
		}
		if (crs == null) {
			throw new ParseException ("Missing CRS for BoundingBox");
		}
		
		boundingBoxes.put (crs, new BoundingBox (parseDouble (reader, "minx"), parseDouble (reader, "miny"), parseDouble (reader, "maxx"), parseDouble (reader, "maxy")));
	}
	
	private static double parseDouble (final XMLStreamReader reader, final String propertyName) throws ParseException {
		final String value = getAttributeValue (reader, propertyName);
		if (value == null) {
			throw new ParseException (String.format ("Missing property: %s", propertyName));
		}
		
		try {
			return Double.parseDouble (value);
		} catch (NumberFormatException e) {
			throw new ParseException (String.format ("Invalid value %s for property %s", value, propertyName));
		}
	}
	
	private static boolean isEnd (final XMLStreamReader reader, final String localName) throws XMLStreamException {
		return !reader.hasNext () || (reader.isEndElement () && localName.equals (reader.getLocalName ()));
	}
	
	private static boolean is (final XMLStreamReader reader, final String localName) {
		return reader.isStartElement () && localName.equals (reader.getLocalName ());
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
