package nl.idgis.geoide.documentcache;

import java.net.URI;

import nl.idgis.ogc.util.MimeContentType;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

public interface Document {
	URI getUri ();
	MimeContentType getContentType ();
	Publisher<ByteString> getBody ();
}
