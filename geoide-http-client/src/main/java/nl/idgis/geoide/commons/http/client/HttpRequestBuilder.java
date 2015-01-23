package nl.idgis.geoide.commons.http.client;

import org.reactivestreams.Publisher;

import play.libs.F.Promise;
import akka.util.ByteString;

public interface HttpRequestBuilder {

	HttpRequestBuilder setUrl (String url);
	HttpRequestBuilder setMethod (HttpRequest.Method method);
	HttpRequestBuilder setTimeoutInMillis (long timeoutInMillis);
	HttpRequestBuilder setFollowRedirects (boolean followRedirects);
	HttpRequestBuilder setParameter (String name, String value);
	HttpRequestBuilder setHeader (String name, String value);
	HttpRequestBuilder setBody (Publisher<ByteString> body);
	
	Promise<HttpResponse> execute (HttpRequest.Method method);
	Promise<HttpResponse> execute ();
	Promise<HttpResponse> get ();
	Promise<HttpResponse> post ();
	Promise<HttpResponse> post (Publisher<ByteString> body);
}
