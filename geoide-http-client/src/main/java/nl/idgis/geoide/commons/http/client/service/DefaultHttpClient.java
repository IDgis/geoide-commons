package nl.idgis.geoide.commons.http.client.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.HttpRequest;
import nl.idgis.geoide.commons.http.client.HttpRequestBuilder;
import nl.idgis.geoide.commons.http.client.HttpResponse;
import nl.idgis.geoide.util.ConfigWrapper;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.api.libs.ws.DefaultWSClientConfig;
import play.api.libs.ws.WSClientConfig;
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder;
import play.api.libs.ws.ssl.SSLConfig;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

import com.ning.http.client.AsyncHttpClientConfig;
import com.typesafe.config.ConfigFactory;

/**
 * Implementation of {@link HttpClient} that uses Play WS to perform the requests (which in turn
 * uses async-http-client). Note that responses produced by this HTTP client are not processed
 * asynchronously. The implementation blocks on read operations on the response input stream.
 */
public class DefaultHttpClient implements HttpClient {

	private final int streamBlockSizeInBytes;
	private final long streamTimeoutInMillis;
	private final StreamProcessor streamProcessor;
	private final WSClient wsClient;

	/**
	 * Creates a DefaultHttpClient by providing references to components on which it depends.
	 * 
	 * @param streamProcessor The stream processor to use for creating reactive streams.
	 * @param streamBlockSizeInBytes The block size to use when reading the HTTP response. Controls the length of blocking read operations on the response.
	 * @param streamTimeoutInMillis The timeout value to use on the response streams (only measures inactivity on the stream). The underlying HTTP response is closed after this timeout expires, even if the consumer didn't read the entire stream.
	 */
	public DefaultHttpClient (final StreamProcessor streamProcessor, final WSClient wsClient, final int streamBlockSizeInBytes, final long streamTimeoutInMillis) {
		if (streamProcessor == null) {
			throw new NullPointerException ("streamProcessor cannot be null");
		}
		if (wsClient == null) {
			throw new NullPointerException ("wsClient cannot be null");
		}
		
		this.streamProcessor = streamProcessor;
		this.wsClient = wsClient;
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
	public CompletableFuture<HttpResponse> request (final HttpRequest request) {
		if (request == null) {
			throw new NullPointerException ("request cannot be null");
		}
		
		WSRequestHolder holder = wsClient
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
	
	private CompletableFuture<HttpResponse> requestWithBody (final WSRequestHolder holder, final HttpRequest request) {
		final CompletableFuture<ByteString> reducedFuture = streamProcessor.reduce (request.getBody (), ByteStrings.empty (), new BiFunction<ByteString, ByteString, ByteString> () {
			@Override
			public ByteString apply (final ByteString a, final ByteString b) {
				return a.concat (b);
			}
		});
		
		return reducedFuture.thenCompose ((data) -> {
			return processWsResponse (holder.setBody (data.iterator ().asInputStream ()).execute ());
		});
	}
	
	private CompletableFuture<HttpResponse> processWsResponse (final Promise<WSResponse> response) {
		final DefaultHttpClient self = this;
		final Promise<HttpResponse> mappedPromise = response.map (new Function<WSResponse, HttpResponse> () {
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
		
		final CompletableFuture<HttpResponse> future = new CompletableFuture<> ();
		
		mappedPromise.onFailure ((throwable) -> future.completeExceptionally (throwable));
		mappedPromise.onRedeem ((wsResponse) -> future.complete (wsResponse));
		
		return future;
	}

	public static WSClient createWSClient () {
		return createWSClient (new ConfigWrapper (ConfigFactory.empty ()));
	}
	
	public static WSClient createWSClient (final ConfigWrapper config) {
		// Set up the client config (you can also use a parser here):
		 scala.Option<Object> none = scala.None$.empty();
		 scala.Option<String> noneString = scala.None$.empty();
		 scala.Option<SSLConfig> noneSSLConfig = scala.None$.empty();
		 WSClientConfig clientConfig = new DefaultWSClientConfig(
		         none, // connectionTimeout
		         none, // idleTimeout
		         none, // requestTimeout
		         none, // followRedirects
		         none, // useProxyProperties
		         noneString, // userAgent
		         none, // compressionEnabled
		         none, // acceptAnyCertificate
		         noneSSLConfig);

		 // Build a secure config out of the client config and the ning builder:
		 AsyncHttpClientConfig.Builder asyncHttpClientBuilder = new AsyncHttpClientConfig.Builder();
		 NingAsyncHttpClientConfigBuilder secureBuilder = new NingAsyncHttpClientConfigBuilder(clientConfig,
		         asyncHttpClientBuilder);
		 AsyncHttpClientConfig secureDefaults = secureBuilder.build();

		 // You can directly use the builder for specific options once you have secure TLS defaults...
		 AsyncHttpClientConfig customConfig = new AsyncHttpClientConfig.Builder(secureDefaults)
			.setCompressionEnabled(true)
			.build();
		
		 return new play.libs.ws.ning.NingWSClient (customConfig);
	}
}
