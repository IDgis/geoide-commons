package nl.idgis.geoide.commons.http.client.service;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.util.streams.StreamProcessor;
import akka.actor.ActorRefFactory;

/**
 * Runs tests on {@link DefaultHttpClient} using tests provided by {@link AbstractTestHttpClient}.
 */
public class TestDefaultHttpClient extends AbstractTestHttpClient {

	/**
	 * Returns an instance of {@link DefaultHttpClient}.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected HttpClient createHttpClient (final ActorRefFactory actorSystem, final StreamProcessor streamProcessor) {
		return new DefaultHttpClient (streamProcessor, 10, 1000);
	}
}
