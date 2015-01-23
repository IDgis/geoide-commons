package nl.idgis.geoide.commons.http.client.service;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.util.streams.StreamProcessor;
import akka.actor.ActorRefFactory;

public class TestDefaultHttpClient extends AbstractTestHttpClient {

	@Override
	protected HttpClient createHttpClient(ActorRefFactory actorSystem,
			StreamProcessor streamProcessor) {
		return new DefaultHttpClient (streamProcessor, 10, 1000);
	}

}
