package nl.idgis.geoide.documentcache.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.DocumentStore;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.document.DocumentCacheException;
import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.HttpRequestBuilder;
import nl.idgis.geoide.util.Futures;

import org.reactivestreams.Publisher;

import play.Logger;
import akka.util.ByteString;

/**
 * An implementation of {@link DocumentStore} that uses a {@link HttpClient} to retrieve documents
 * by turning the URI into a request URL.
 * 
 * Uses a reference to an existing HttpClient and is configured with a timeout value that is used for all
 * requests initiated by this store. The store doesn't perform caching, but it can be used as a readthrough
 * store for a {@link DocumentCache}.
 */
public class HttpDocumentStore implements DocumentStore {

	private final HttpClient httpClient;
	private final long timeoutInMillis;
	
	/**
	 * Creates a new document store for the given {@link HttpClient} with a default timeout
	 * value of 60 seconds.
	 * 
	 * @param httpClient The HTTP client to be used by this component for all outbound HTTP traffic.
	 */
	public HttpDocumentStore (final HttpClient httpClient) {
		this (httpClient, 60000);
	}

	/**
	 * Creates a new document store by providing a {@link HttpClient} and a timeout value.
	 * 
	 * @param httpClient		The HTTP client to be used by this component for all outbound HTTP traffic.
	 * @param timeoutInMillis	The timeout in milliseconds to use for each HTTP request.
	 */
	public HttpDocumentStore (final HttpClient httpClient, final long timeoutInMillis) {
		if (httpClient == null) {
			throw new NullPointerException ("httpClient cannot be null");
		}
		
		this.httpClient = httpClient;
		this.timeoutInMillis = timeoutInMillis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Document> fetch (final URI uri) {
		if (!"http".equals (uri.getScheme ()) && !"https".equals (uri.getScheme ())) {
			Logger.debug ("Bad scheme: " + uri.toString ());
			return Futures.throwing (new DocumentCacheException.DocumentNotFoundException (uri)); 
		}

		final URI shortUri;
		try {
			shortUri = new URI (uri.getScheme (), uri.getUserInfo (), uri.getHost (), uri.getPort (), uri.getPath (), null, null);
		} catch (URISyntaxException e) {
			return Futures.throwing (e);
		}
		HttpRequestBuilder builder = httpClient
			.url (shortUri.toString ())
			.setFollowRedirects (true)
			.setTimeoutInMillis (timeoutInMillis);
		
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
					return Futures.throwing (e);
				}
				
				builder = builder.setParameter (key, value);
			}
		}

		return builder
			.get ()
			.thenApply ((response) -> {
				if (response.getStatus () < 200 || response.getStatus () >= 300) {
					Logger.debug ("Document not found: " + response.getStatus () + " " + response.getStatusText ());
					throw new DocumentCacheException.DocumentNotFoundException (uri);
				}

				final MimeContentType contentType = new MimeContentType (response.getHeader ("Content-Type"));
				final Publisher<ByteString> body = response.getBody ();
				
				return (Document)new Document () {
					@Override
					public URI getUri () {
						return uri;
					}
					
					@Override
					public MimeContentType getContentType () {
						return contentType;
					}
					
					@Override
					public Publisher<ByteString> getBody () {
						return body;
					}
				};
			});
	}
}
