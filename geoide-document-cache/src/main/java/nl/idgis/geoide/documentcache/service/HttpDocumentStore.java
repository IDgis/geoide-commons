package nl.idgis.geoide.documentcache.service;

import java.io.InputStream;
import java.net.URI;

import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;
import nl.idgis.geoide.documentcache.ByteStringCachedDocument;
import nl.idgis.geoide.documentcache.CachedDocument;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.ogc.util.MimeContentType;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

public class HttpDocumentStore implements DocumentStore {

	private final long timeoutInMillis;
	
	public HttpDocumentStore () {
		this (20000);
	}
	
	public HttpDocumentStore (final long timeoutInMillis) {
		this.timeoutInMillis = timeoutInMillis;
	}
	
	@Override
	public Promise<CachedDocument> fetch (final URI uri) {
		if (!"http".equals (uri.getScheme ()) && !"https".equals (uri.getScheme ())) {
			return Promise.throwing (new DocumentCacheException.DocumentNotFoundException (uri)); 
		}

		return WS
			.url (uri.toString ())
			.setFollowRedirects (true)
			.setTimeout ((int) timeoutInMillis)
			.get ()
			.map (new Function<WSResponse, CachedDocument> () {
				@Override
				public CachedDocument apply (final WSResponse response) throws Throwable {
					if (response.getStatus () < 200 || response.getStatus () >= 300) {
						throw new DocumentCacheException.DocumentNotFoundException (uri);
					}

					ByteString byteString = ByteStrings.empty ();
					final InputStream inputStream = response.getBodyAsStream ();
					
					byte[] data = new byte[4096];
					int nRead;
					
					while ((nRead = inputStream.read (data, 0, data.length)) != -1) {
						byteString = byteString.concat (ByteStrings.fromArray (data, 0, nRead));
					}
					
					inputStream.close ();
					
					return new ByteStringCachedDocument (
							uri, 
							new MimeContentType (response.getHeader ("Content-Type")), 
							byteString
						);
				}
			});
	}
}
