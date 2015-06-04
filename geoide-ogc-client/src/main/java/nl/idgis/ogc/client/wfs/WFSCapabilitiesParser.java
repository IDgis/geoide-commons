package nl.idgis.ogc.client.wfs;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.ogc.wfs.WFSCapabilities;
import nl.idgis.ogc.wfs.WFSCapabilities.FeatureType;
import nl.idgis.ogc.wfs.WFSCapabilities.Operation;
import nl.idgis.ogc.wfs.WFSCapabilities.OperationType;
import nl.idgis.ogc.wfs.WFSCapabilities.ServiceIdentification;

public class WFSCapabilitiesParser {

	private final static String WFS = "http://www.opengis.net/wfs";
	private final static String OWS = "http://www.opengis.net/ows";
	private final static String WFS_20 = "http://www.opengis.net/wfs/2.0";
	private final static String OWS_11 = "http://www.opengis.net/ows/1.1";
	private final static String XLINK = "http://www.w3.org/1999/xlink";
	
	public static WFSCapabilities parseCapabilities (final InputStream inputStream) throws ParseException {
		try {
			final XMLStreamReader reader = XMLInputFactory.newFactory ().createXMLStreamReader (inputStream);
			
			// Skip to the root tag:
			reader.nextTag ();
			
			if (!"WFS_Capabilities".equals (reader.getLocalName ())) {
				throw new ParseException (String.format ("Not a WFS capabilities document (root tag: %s)", reader.getLocalName ())); 
			}
			
			if (WFS_20.equals (reader.getNamespaceURI ())) {
				return parseCapabilities (reader, WFS_20, OWS_11, "2.0.0");
			} else if (WFS.equals (reader.getNamespaceURI ())) {
				return parseCapabilities (reader, WFS, OWS, "1.1.0");
			} else {
				throw new ParseException (String.format ("Unknown WFS version: %s (namespace: %s)", reader.getAttributeValue (null, "version"), reader.getNamespaceURI ()));
			}
		} catch (XMLStreamException e) {
			throw new ParseException ("Error parsing XML stream", e);
		} catch (FactoryConfigurationError e) {
			throw new ParseException ("Error parsing XML stream", e);
		}
	}
	
	private static WFSCapabilities parseCapabilities (final XMLStreamReader reader, final String nsWfs, final String nsOws, final String expectedVersion) throws ParseException, XMLStreamException {
		final String version = reader.getAttributeValue (null, "version");
		
		if (version == null) {
			throw new ParseException ("WFS capabilities document does not specify a version");
		}
		if (!expectedVersion.equals (version)) {
			throw new ParseException (String.format ("WFS version %s does not match the namespace %s (expected version %s)", version, nsWfs, expectedVersion));
		}
		
		ServiceIdentification serviceIdentification = null;
		List<Operation> operations = null;
		List<FeatureType> featureTypes = null;
		
		while (reader.hasNext ()) {
			// Skip to the next tag:
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}
			
			if (nsOws.equals (reader.getNamespaceURI ()) && "ServiceIdentification".equals (reader.getLocalName ())) {
				serviceIdentification = parseServiceIdentification (reader, nsWfs, nsOws);
			} else if (nsOws.equals (reader.getNamespaceURI ()) && "OperationsMetadata".equals (reader.getLocalName ())) {
				operations = parseOperationsMetadata (reader, nsWfs, nsOws);
			} else if (nsWfs.equals (reader.getNamespaceURI ()) && "FeatureTypeList".equals (reader.getLocalName ())) {
				featureTypes = parseFeatureTypes (reader, nsWfs, nsOws);
			}
		}
		
		if (serviceIdentification == null) {
			throw new ParseException ("Capabilities document did not specify service identification");
		}
		if (operations == null) {
			throw new ParseException ("Capabilities document did not specify service operations metadata");
		}
		if (featureTypes == null) {
			throw new ParseException ("Capabilities document did not specify feature types");
		}

		final WFSCapabilities capabilities = new WFSCapabilities (version, serviceIdentification, operations, featureTypes);
		validateCapabilities (capabilities);
		return capabilities;
	}

	private static void validateCapabilities (final WFSCapabilities capabilities) throws ParseException {
		// Make sure each feature type has at least one output format:
		for (final FeatureType featureType: capabilities.featureTypes ()) {
			if (featureType.outputFormats ().isEmpty ()) {
				throw new ParseException (String.format ("Feature type %s has no valid output formats", featureType.name ()));
			}
		}
	}
	
	private static ServiceIdentification parseServiceIdentification (final XMLStreamReader reader, final String nsWfs, final String nsOws) throws XMLStreamException, ParseException {
		String title = null;
		String abstractText = null;
		List<String> versions = new ArrayList<String> ();
		
		while (!isEnd (reader, nsOws, "ServiceIdentification")) {
			reader.next ();
			if (!reader.isStartElement ()) {
				continue;
			}
			
			if (is (reader, nsOws, "Title")) {
				title = reader.getElementText ();
			} else if (is (reader, nsOws, "Abstract")) {
				abstractText = reader.getElementText ();
			} else if (is (reader, nsOws, "ServiceTypeVersion")) {
				versions.add (reader.getElementText ());
			}
		}

		if (title == null) {
			throw new ParseException ("Service identification did not specify a title");
		}
		if (versions.isEmpty ()) {
			throw new ParseException ("Service identification did not specify any versions");
		}
		
		return new ServiceIdentification (title, abstractText, versions);
	}
	
	private static List<Operation> parseOperationsMetadata (final XMLStreamReader reader, final String nsWfs, final String nsOws) throws ParseException, XMLStreamException {
		final List<Operation> operations = new ArrayList<WFSCapabilities.Operation> ();
		
		while (!isEnd (reader, nsOws, "OperationsMetadata")) {
			reader.next ();
			
			if (is (reader, nsOws, "Operation")) {
				final Operation operation = parseOperation (reader, nsWfs, nsOws);
				if (operation != null) {
					operations.add (operation);
				}
			}
		}
		
		if (operations.isEmpty ()) {
			throw new ParseException ("WFS capabilities document did not specify any operations");
		}
		
		return operations;
	}
	
	private static Operation parseOperation (final XMLStreamReader reader, final String nsWfs, final String nsOws) throws ParseException, XMLStreamException {
		final String name = reader.getAttributeValue (null, "name");
		
		if (name == null) {
			throw new ParseException ("Operation without a name");
		}
		
		final OperationType operationType = getOperationType (name);
		if (operationType == null) {
			// Skip unknown operations:
			return null;
		}
		
		String httpGet = null;
		String httpPost = null;
		Set<MimeContentType> outputFormats = null;
		
		while (!isEnd (reader, nsOws, "Operation")) {
			reader.next ();
			
			if (is (reader, nsOws, "Get")) {
				httpGet = parseHref (reader);
			} else if (is (reader, nsOws, "Post")) {
				httpPost = parseHref (reader);
			} else if (is (reader, nsOws, "Parameter") && "outputFormat".equals (reader.getAttributeValue (null, "name"))) {
				outputFormats = parseOutputFormats (reader, nsOws);
			}
		}
		
		if (httpGet == null && httpPost == null) {
			throw new ParseException (String.format ("Operation %s provided neither a get nor a post URI", name));
		}
		
		return new Operation (operationType, httpGet, httpPost, outputFormats);
	}
	
	private static Set<MimeContentType> parseOutputFormats (final XMLStreamReader reader, final String nsOws)  throws ParseException, XMLStreamException {
		final Set<MimeContentType> outputFormats = new HashSet<> ();
		
		while (!isEnd (reader, nsOws, "Parameter")) {
			reader.next ();
			
			if (is (reader, nsOws, "Value")) {
				final String value = reader.getElementText ();
				
				if (value != null && MimeContentType.isValid (value.trim ())) {
					outputFormats.add (new MimeContentType (value));
				}
			}
		}
		
		return outputFormats;
	}
	
	private static String parseHref (final XMLStreamReader reader) throws ParseException, XMLStreamException {
		final String href = reader.getAttributeValue (XLINK, "href");
		if (href == null) {
			throw new ParseException ("XLink without href attribute");
		}
		
		return href;
	}
	
	private static OperationType getOperationType (final String name) {
		for (final OperationType ot: OperationType.values ()) {
			if (name.equals (ot.operationName ())) {
				return ot;
			}
		}
		return null;
	}
	
	private static List<FeatureType> parseFeatureTypes (final XMLStreamReader reader, final String nsWfs, final String nsOws) throws ParseException, XMLStreamException {
		final List<FeatureType> featureTypes = new ArrayList<FeatureType> ();
	
		while (!isEnd (reader, nsWfs, "FeatureTypeList")) {
			reader.next ();
			
			if (is (reader, nsWfs, "FeatureType")) {
				final FeatureType featureType = parseFeatureType (reader, nsWfs);
				if (featureType != null) {
					featureTypes.add (featureType);
				}
			}
		}
		
		if (featureTypes.isEmpty ()) {
			throw new ParseException ("WFS capabilities document did not specify any feature types");
		}
		
		return featureTypes;
	}
	
	private static FeatureType parseFeatureType (final XMLStreamReader reader, final String nsWfs) throws ParseException, XMLStreamException {
		String name = null;
		String namespaceUri = null;
		String namespacePrefix = null;
		String title = null;
		String defaultCRS = null;
		List<String> otherCRS = new ArrayList<String> ();
		List<MimeContentType> outputFormats = new ArrayList<> ();
		
		while (!isEnd (reader, nsWfs, "FeatureType")) {
			reader.next ();
			
			if (is (reader, nsWfs, "Name")) {
				final String fqn = reader.getElementText ();
				final int offset = fqn.indexOf (':');
				
				if (offset == 0) {
					throw new ParseException (String.format ("Malformed feature type name: %s", fqn));
				} else if (offset > 0) { 
					final String prefix = fqn.substring (0, offset);
					
					name = fqn.substring (offset + 1);
					namespaceUri = reader.getNamespaceContext ().getNamespaceURI (prefix);
					namespacePrefix = prefix;
					
					if (XMLConstants.NULL_NS_URI.equals (namespaceUri)) {
						throw new ParseException (String.format ("Feature type %s has an unbound namespace prefix %s", fqn, prefix));
					}
				} else {
					name = fqn;
					namespaceUri = null;
					namespacePrefix = null;
				}
				
			} else if (is (reader, nsWfs, "Title")) {
				title = reader.getElementText ();
			} else if (is (reader, nsWfs, "DefaultCRS") || is (reader, nsWfs, "DefaultSRS")) {
				defaultCRS = reader.getElementText ();
			} else if (is (reader, nsWfs, "OtherCRS") || is (reader, nsWfs, "OtherSRS")) {
				otherCRS.add (reader.getElementText ());
			} else if (is (reader, nsWfs, "Format")) {
				final String format = reader.getElementText ();
				if (!MimeContentType.isValid (format)) {
					//Do not throw error but just skip
					//throw new ParseException (String.format ("Feature type %s has an illegal format %s", name, format));
				} else {
					outputFormats.add (new MimeContentType (format));
				}
			}
		}
		
		if (name == null) {
			throw new ParseException ("Feature type did not specify a name");
		}
		if (defaultCRS == null) {
			throw new ParseException (String.format ("Feature type %s did not specify a default CRS", name));
		}
		
		return new FeatureType (name, namespaceUri, namespacePrefix, title, defaultCRS, otherCRS, outputFormats);
	}
	
	private static boolean is (final XMLStreamReader reader, final String ns, final String localName) {
		return reader.isStartElement () && ns.equals (reader.getNamespaceURI ()) && localName.equals (reader.getLocalName ());
	}
	
	private static boolean isEnd (final XMLStreamReader reader, final String ns, final String localName) throws XMLStreamException {
		return !reader.hasNext () || (reader.isEndElement () && ns.equals (reader.getNamespaceURI ()) && localName.equals (reader.getLocalName ()));
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
