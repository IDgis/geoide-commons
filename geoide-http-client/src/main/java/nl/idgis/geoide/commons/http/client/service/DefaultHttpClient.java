package nl.idgis.geoide.commons.http.client.service;

import java.util.List;
import java.util.Map;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.HttpRequest;
import nl.idgis.geoide.commons.http.client.HttpRequestBuilder;
import nl.idgis.geoide.commons.http.client.HttpResponse;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.libs.F.Function;
import play.libs.F.Function2;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

/**
 * Implementation of {@link HttpClient} that uses Play WS to perform the requests (which in turn
 * uses async-http-client). Note that responses produced by this HTTP client are not processed
 * asynchronously. The implementation blocks on read operations on the response input stream.
 */
public class DefaultHttpClient implements HttpClient {

	private final int streamBlockSizeInBytes;
	private final long streamTimeoutInMillis;
	private final StreamProcessor streamProcessor;

	/**
	 * Creates a DefaultHttpClient by providing references to components on which it depends.
	 * 
	 * @param streamProcessor The stream processor to use for creating reactive streams.
	 * @param streamBlockSizeInBytes The block size to use when reading the HTTP response. Controls the length of blocking read operations on the response.
	 * @param streamTimeoutInMillis The timeout value to use on the response streams (only measures inactivity on the stream). The underlying HTTP response is closed after this timeout expires, even if the consumer didn't read the entire stream.
	 */
	public DefaultHttpClient (final StreamProcessor streamProcessor, final int streamBlockSizeInBytes, final long streamTimeoutInMillis) {
		if (streamProcessor == null) {
			throw new NullPointerException ("streamProcessor cannot be null");
		}
		
		this.streamProcessor = streamProcessor;
		this.streamBlockSizeInBytes = streamBlockSizeInBytes;
		this.streamTimeoutInMillis = streamTimeoutInMillis;
	}

	/**
	 * @see HttpClient#request()
	 */
	@Override
	public HttpRequestBuilder request () {
		return new DefaultHttpRequestBuilder (this);
	}
	
	/**
	 * @see HttpClient#url(String)
	 */
	@Override
	public HttpRequestBuilder url (final String url) {
		return request ().setUrl (url);
	}
	
	/**
	 * @see HttpClient#request(HttpRequest)
	 */
	@Override
	public Promise<HttpResponse> request (final HttpRequest request) {
		if (request == null) {
			throw new NullPointerException ("request cannot be null");
		}
		
		WSRequestHolder holder = WS
				.url (request.getUrl ())
				.setTimeout ((int) request.getTimeoutInMillis ())
				.setFollowRedirects (request.isFollowRedirects ())
				.setMethod (request.getMethod ().name ());

		// Add query parameters:
		for (final Map.Entry<String, List<String>> entry: request.getParameters ().entrySet ()) {
			if (entry.getValue () == null) {
				continue;
			}
			
			for (final String value: entry.getValue ()) {
				holder = holder.setQueryParameter (entry.getKey (), value);
			}
		}
		
		// Add headers:
		for (final Map.Entry<String, List<String>> entry: request.getHeaders ().entrySet ()) {
			if (entry.getValue () == null) {
				continue;
			}
			
			for (final String value: entry.getValue ()) {
				holder = holder.setHeader (entry.getKey (), value);
			}
		}

		// Execute the request with or without a body:
		if (request.getBody () != null) {
			return requestWithBody (holder, request);
		} else {
			return processWsResponse (holder.execute ());			
		}
	}
	
	private Promise<HttpResponse> requestWithBody (final WSRequestHolder holder, final HttpRequest request) {
		final Promise<ByteString> reducedPromise = streamProcessor.reduce (request.getBody (), ByteStrings.empty (), new Function2<ByteString, ByteString, ByteString> () {
			@Override
			public ByteString apply (final ByteString a, final ByteString b) throws Throwable {
				return a.concat (b);
			}
		});
		
		return reducedPromise.flatMap (new Function<ByteString, Promise<HttpResponse>> () {
			@Override
			public Promise<HttpResponse> apply (final ByteString data) throws Throwable {
				return processWsResponse (holder.setBody (data.iterator ().asInputStream ()).execute ());
			}
		});
	}
	
	private Promise<HttpResponse> processWsResponse (final Promise<WSResponse> response) {
		final DefaultHttpClient self = this;
		return response.map (new Function<WSResponse, HttpResponse> () {
			@Override
			public HttpResponse apply (final WSResponse response) throws Throwable {
				return new DefaultHttpResponse (
						self, 
						response.getStatus (), 
						response.getStatusText (), 
						response.getAllHeaders (), 
						streamProcessor.publishInputStream (
								response.getBodyAsStream (), 
								streamBlockSizeInBytes, 
								streamTimeoutInMillis
							)
					);
			}
		});
	}
}
