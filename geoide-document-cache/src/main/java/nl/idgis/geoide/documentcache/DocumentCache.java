package nl.idgis.geoide.documentcache;

import java.io.InputStream;
import java.net.URI;

import nl.idgis.ogc.util.MimeContentType;

import org.reactivestreams.Publisher;

import play.libs.F.Promise;
import akka.util.ByteString;

public interface DocumentCache extends DocumentStore {
	Promise<Long> getTtl ();
	
	Promise<CachedDocument> store (URI uri);
	Promise<CachedDocument> store (URI uri, MimeContentType contentType, byte[] data);
	Promise<CachedDocument> store (URI uri, MimeContentType contentType, InputStream inputStream);
	Promise<CachedDocument> store (URI uri, MimeContentType contentType, Publisher<ByteString> body);
}
