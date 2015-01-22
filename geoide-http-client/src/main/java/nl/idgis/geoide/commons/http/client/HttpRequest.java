package nl.idgis.geoide.commons.http.client;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

public interface HttpRequest {
	Method getMethod ();
	String getUrl ();
	long getTimeoutInMillis ();
	boolean isFollowRedirects ();
	Map<String, List<String>> getParameters ();
	Map<String, List<String>> getHeaders ();

	Publisher<ByteString> getBody ();

	public static enum Method {
		GET,
		PATCH,
		POST,
		PUT,
		DELETE,
		HEAD,
		OPTIONS
	}
}
