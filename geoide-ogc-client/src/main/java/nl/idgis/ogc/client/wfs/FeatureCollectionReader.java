package nl.idgis.ogc.client.wfs;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.idgis.ogc.util.MimeContentType;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML2GeometryReader;
import org.deegree.gml.geometry.GML3GeometryReader;
import org.deegree.gml.geometry.GMLGeometryReader;

public class FeatureCollectionReader {
	
	private final GMLVersion gmlVersion;
	
	final static Set<String> GML_NAMESPACES = new HashSet<String> ();
	static {
		GML_NAMESPACES.add ("http://www.opengis.net/gml");
		GML_NAMESPACES.add ("http://www.opengis.net/gml/3.2");
	}
	
	final static Set<String> WFS_NAMESPACES = new HashSet<String> ();
	static {
		WFS_NAMESPACES.add ("http://www.opengis.net/wfs/2.0");
		WFS_NAMESPACES.add ("http://www.opengis.net/wfs");
	}
	
	private abstract class AbstractFeatureCollection implements FeatureCollection {
	}
	
	private class DefaultFeatureCollection extends AbstractFeatureCollection {		
		boolean parsing = false, endOfStream = false;
				
		final XMLStreamReader streamReader;
		final boolean featureMembers;
		
		private final GMLVersion gmlVersion;

		public DefaultFeatureCollection(XMLStreamReader streamReader, boolean featureMembers, final GMLVersion gmlVersion) {
			this.streamReader = streamReader;
			this.featureMembers = featureMembers;
			this.gmlVersion = gmlVersion;
		}
		
		@Override
		public Iterator<Feature> iterator() {
			if(parsing) {
				throw new IllegalStateException("Parsing already started");
			}
			
			parsing = true;
			
			try {
				return new Iterator<Feature>() {
					
					{							
						if(featureMembers) {
							nextTag();
						} else {
							nextFeatureMember();
						}
					}
					
					private void nextTag() throws XMLStreamException {
						streamReader.next();
						while(streamReader.hasNext() && !streamReader.isStartElement()) {
							streamReader.next();
						}
					}
					
					private void nextFeatureMember() throws XMLStreamException {
						if(featureMembers) {
							
							while(streamReader.hasNext()) {
								if(streamReader.isStartElement()) {
									break;
								}
								
								if(streamReader.isEndElement() 
									&& GML_NAMESPACES.contains (streamReader.getNamespaceURI())
									&& streamReader.getLocalName().equals("featureMembers")) {
									
									endOfStream = true;
									break;
								}
								
								streamReader.next();
							}
						} else {							
							while(streamReader.hasNext()) {
								if(streamReader.isStartElement()) {
									QName currentName = streamReader.getName();
									if( (GML_NAMESPACES.contains (currentName.getNamespaceURI()) || WFS_NAMESPACES.contains (currentName.getNamespaceURI ()))
										&& (currentName.getLocalPart().equals("featureMember") || currentName.getLocalPart ().equals ("member"))) {										
										nextTag();											
										
										return;										
									}
								}
								
								streamReader.next();
							}
							
							endOfStream = true;
						}
					}

					@Override
					public boolean hasNext() {
						return !endOfStream;
					}

					@Override
					public Feature next() {
						try {
							final Feature feature = parseFeature ();
							
							nextFeatureMember ();
							
							return feature;
						} catch(Exception e) {
							throw new ParseException ("Couldn't read feature member", e);
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					private Feature parseFeature () throws XMLStreamException, ParseException, XMLParsingException, UnknownCRSException {
						final String featureTypeName = streamReader.getLocalName ();
						final String featureTypeNamespace = streamReader.getNamespaceURI ();
						final Map<String, Object> attributes = new HashMap<> ();
						
						// Parse the ID:
						final String id;
						if (gmlVersion.equals (GMLVersion.GML_2)) {
							id = streamReader.getAttributeValue (null, "fid");
						} else {
							id = streamReader.getAttributeValue (gmlVersion.getNamespace (), "id");
						}
						
						while (streamReader.hasNext ()) {
							streamReader.next ();
							while (streamReader.hasNext () && !(streamReader.isStartElement () || streamReader.isEndElement ())) {
								streamReader.next ();
							}
							
							// Stop parsing at the end of the feature:
							if (streamReader.isEndElement () && streamReader.getLocalName ().equals (featureTypeName)) {
								break;
							}
							
							if (streamReader.isStartElement () && ((streamReader.getNamespaceURI () == null && featureTypeNamespace == null) || (streamReader.getNamespaceURI () != null && streamReader.getNamespaceURI().equals (featureTypeNamespace)))) {
								attributes.put (streamReader.getLocalName (), parseAttributeValue (streamReader.getNamespaceURI (), streamReader.getLocalName ()));
							}
						}
						
						if (id == null) {
							throw new ParseException ("Feature must have an ID property");
						}
						if (featureTypeName == null) {
							throw new ParseException ("Feature type could not be determined");
						}
						
						return new Feature (featureTypeName, featureTypeNamespace, id, attributes);
					}
					
					private Object parseAttributeValue (final String namespace, final String localName) throws XMLStreamException, XMLParsingException, UnknownCRSException {
						final StringBuilder builder = new StringBuilder ();
						Geometry geometry = null;
						
						while (streamReader.hasNext ()) {
							streamReader.next ();
							
							if (streamReader.isEndElement () && ((streamReader.getNamespaceURI () == null && namespace == null) || (streamReader.getNamespaceURI () != null && streamReader.getNamespaceURI ().equals (namespace)))) {
								break;
							}
							
							if (streamReader.isStartElement () && gmlVersion.getNamespace ().equals (streamReader.getNamespaceURI ())) {
								// Parse geometry:
								final XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper (streamReader, "");
								final GMLGeometryReader reader;
								if (gmlVersion.equals (GMLVersion.GML_2)) {
									reader = new GML2GeometryReader (GMLInputFactory.createGMLStreamReader (gmlVersion, wrapper));
								} else {
									reader = new GML3GeometryReader (GMLInputFactory.createGMLStreamReader (gmlVersion, wrapper));
								}
								
								geometry = reader.parse (wrapper, null);
								
							} else if (streamReader.isCharacters() || streamReader.isWhiteSpace ()) {
								// Parse text:
								builder.append (streamReader.getText ());
							}
						}
						
						if (geometry != null) {
							return geometry;
						}
						
						return builder.toString ().trim ();
					}
				};
			} catch(Exception e) {
				throw new ParseException("Couldn't parse feature collection", e);
			}
		}
	}
	
	private class EmptyFeatureCollection extends AbstractFeatureCollection {

		@Override
		public Iterator<Feature> iterator() {
			return Collections.<Feature>emptyList().iterator();
		}
	}
	
	public FeatureCollectionReader (final MimeContentType format) {
		if (format == null) {
			throw new NullPointerException ("format cannot be null");
		}
		
		GMLVersion gmlVersion = null;
		for (final Map.Entry<String, String> entry: format.parameters ().entrySet ()) {
			if (!"subtype".equals (entry.getKey ().toLowerCase ())) {
				continue;
			}
			if (!entry.getValue ().startsWith ("gml/")) {
				continue;
			}
			
			final String version = entry.getValue ().substring (4);

			if (version.startsWith ("2")) {
				gmlVersion = GMLVersion.GML_2;
			} else if (version.startsWith ("3.0")) {
				gmlVersion = GMLVersion.GML_30;
			} else if (version.startsWith ("3.1")) {
				gmlVersion = GMLVersion.GML_31;
			} else if (version.startsWith ("3.2")) {
				gmlVersion = GMLVersion.GML_32;
			} else {
				throw new IllegalArgumentException ("Unsupported GML version: " + format.original ());
			}
			
			break;
		}
		
		if (gmlVersion == null) {
			throw new IllegalArgumentException ("Invalid GML version: " + format.original ());
		}
		
		this.gmlVersion = gmlVersion; 
	}

	public FeatureCollection parseCollection (final InputStream inputStream) throws ParseException {
		try {
			final XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader (inputStream);
			
			return parseCollection (reader);
		} catch (XMLParsingException | UnknownCRSException | XMLStreamException | FactoryConfigurationError e) {
			throw new ParseException ("Unable to create feature collection", e);
		}
	}
	
	private FeatureCollection parseCollection (final XMLStreamReader streamReader) throws XMLStreamException, XMLParsingException, UnknownCRSException, ParseException {
		if (!streamReader.isStartElement ()) {
			streamReader.nextTag ();
		}
		
		if(streamReader.isStartElement()) {
			QName rootName = streamReader.getName();
			if((WFS_NAMESPACES.contains (rootName.getNamespaceURI()) || GML_NAMESPACES.contains (rootName.getNamespaceURI ()))
				&& rootName.getLocalPart().equals("FeatureCollection")) {									
				while(streamReader.hasNext()) {
					if(streamReader.isStartElement()) {
						final String localName = streamReader.getLocalName();
						if(GML_NAMESPACES.contains (streamReader.getNamespaceURI ())) {
							if(localName.equals("featureMember")) {
								return new DefaultFeatureCollection(streamReader, false, gmlVersion);
							} else if(localName.equals("featureMembers")) {
								return new DefaultFeatureCollection(streamReader, true, gmlVersion);
							}
						} else if (WFS_NAMESPACES.contains (streamReader.getNamespaceURI ())) {
							if (localName.equals ("member")) {
								return new DefaultFeatureCollection (streamReader, false, gmlVersion);
							}
						}
					} else if(streamReader.isEndElement() && streamReader.getName().equals(rootName)) {
						return new EmptyFeatureCollection();
					}
					
					streamReader.next();
				}									
			}
			
			throw new ParseException ("XML stream is not a feature collection");			
		} else {
			throw new ParseException ("Empty XML stream");
		}
	}
	
	protected static Object convertPrimitiveValue (final PrimitiveValue value) {
		final Object val = value.getValue ();
		
		if (val instanceof DateTime) {
			return new Timestamp (((DateTime)val).getTimeInMilliseconds ());
		} else if (val instanceof Date) {
			return new java.sql.Date (((Date)val).getTimeInMilliseconds ());
		}
		
		return val;
	}
	
	public final static class ParseException extends RuntimeException {
		private static final long serialVersionUID = 8012133724843959577L;

		public ParseException (final String message) {
			super (message);
		}
		
		public ParseException (final String message, final Throwable cause) {
			super (message, cause);
		}
	}
}