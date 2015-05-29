package nl.idgis.geoide.commons.http.client.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.HttpRequest;
import nl.idgis.geoide.commons.http.client.HttpRequestBuilder;
import nl.idgis.geoide.commons.http.client.HttpResponse;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

/**
 * EXPERIMENTAL
 * 
 * Experimental {@link HttpClient} implementation using Apache HttpAsyncClient. This is
 * a work in progress and currently shouldn't be used.
 * 
 * The goal is to provide a HTTP client that is completely reactive: both request and
 * response bodies stream with backpressure using reactive pull.
 */
public class StreamingHttpClient implements HttpClient, Closeable {

	private final CloseableHttpAsyncClient client;
	
	public StreamingHttpClient (final long connectionTimeoutMillis) {
		this.client = HttpAsyncClientBuilder
			.create ()
			.build ();
	}
	
	@Override
	public void close () throws IOException {
		client.close ();
	}
	
	@Override
	public HttpRequestBuilder request () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpRequestBuilder url (final String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<HttpResponse> request (final HttpRequest request) {
		final HttpAsyncRequestProducer requestProducer = new HttpAsyncRequestProducer () {
			@Override
			public void close() throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void resetRequest() throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void requestCompleted(HttpContext arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void produceContent(ContentEncoder arg0, IOControl arg1)
					throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isRepeatable() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public HttpHost getTarget() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public org.apache.http.HttpRequest generateRequest() throws IOException,
					HttpException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void failed(Exception arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		final HttpAsyncResponseConsumer<HttpResponse> responseConsumer = new HttpAsyncResponseConsumer<HttpResponse> () {

			@Override
			public void close() throws IOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean cancel() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void consumeContent(ContentDecoder arg0, IOControl arg1)
					throws IOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void failed(Exception arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Exception getException() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public HttpResponse getResult() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isDone() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void responseCompleted(HttpContext arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void responseReceived(org.apache.http.HttpResponse arg0)
					throws IOException, HttpException {
				// TODO Auto-generated method stub
				
			}
		};
		final FutureCallback<HttpResponse> callback = new FutureCallback<HttpResponse> () {
			@Override
			public void cancelled() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void completed(HttpResponse arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void failed(Exception arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		
		client.execute (requestProducer, responseConsumer, callback);
		
		/*
		BoundRequestBuilder builder = client
			.prepareConnect (request.getUrl ())
			.setMethod (request.getMethod ().name ())
			.setFollowRedirects (request.isFollowRedirects ());
		
		for (final Map.Entry<String, List<String>> entry: request.getParameters ().entrySet ()) {
			for (final String value: entry.getValue ()) {
				builder = builder.addQueryParameter (entry.getKey (), value);
			}
		}
		
		for (final Map.Entry<String, List<String>> entry: request.getHeaders ().entrySet ()) {
			for (final String value: entry.getValue ()) {
				builder = builder.addHeader (entry.getKey (), value);
			}
		}
		
		final Publisher<ByteString> body = request.getBody ();
		if (body != null) {
			builder.setBody (dataWriter)
			builder.setBody (createBodyGenerator (body));
		}

		try {
			return execute (request, builder);
		} catch (IOException e) {
			return Promise.throwing (e);
		}
		*/
		
		return null;
	}
}
