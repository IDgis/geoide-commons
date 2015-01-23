package nl.idgis.geoide.commons.http.client;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

public interface HttpResponse {
	int getStatus ();
	String getStatusText ();
	Map<String, List<String>> getHeaders ();
	Publisher<ByteString> getBody ();
	String getHeader (String name);
}
