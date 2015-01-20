package nl.idgis.geoide.documentcache.service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import nl.idgis.geoide.documentcache.ByteStringCachedDocument;
import nl.idgis.geoide.documentcache.CachedDocument;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.ogc.util.MimeContentType;
import play.Logger;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

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
			Logger.debug ("Bad scheme: " + uri.toString ());
			return Promise.throwing (new DocumentCacheException.DocumentNotFoundException (uri)); 
		}

		final URI shortUri;
		try {
			shortUri = new URI (uri.getScheme (), uri.getUserInfo (), uri.getHost (), uri.getPort (), uri.getPath (), null, null);
		} catch (URISyntaxException e) {
			return Promise.throwing (e);
		}
		WSRequestHolder holder = WS
			.url (shortUri.toString ())
			.setFollowRedirects (true)
			.setTimeout ((int) timeoutInMillis);
		
		final String rawQuery = uri.getRawQuery ();
		if (rawQuery != null) {
			final String parts[] = uri.getRawQuery ().split ("\\&");
			for (final String part: parts) {
				final int offset = part.indexOf ('=');
				final String key = part.substring (0, offset);
				final String rawValue = offset > 0 ? part.substring (offset + 1) : "";
				final String value;
				try {
					value = URLDecoder.decode (rawValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					return Promise.throwing (e);
				}
				
				holder = holder.setQueryParameter (key, value);
			}
		}

		return holder
			.get ()
			.map (new Function<WSResponse, CachedDocument> () {
				@Override
				public CachedDocument apply (final WSResponse response) throws Throwable {
					if (response.getStatus () < 200 || response.getStatus () >= 300) {
						Logger.debug ("Document not found: " + response.getStatus () + " " + response.getStatusText ());
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
							byteString.compact ()
						);
				}
			});
	}
}
