package geoide.config;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.service.DefaultHttpClient;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.Play;

@Configuration
public class HttpClientConfig {
	
	@Bean
	@Autowired
	public HttpClient httpClient (final StreamProcessor streamProcessor) {
		return new DefaultHttpClient (
				streamProcessor,
				Play.application ().configuration ().getInt ("geoide.services.httpClient.streamBlockSizeInBytes", 2048).intValue (),
				Play.application ().configuration ().getLong ("geoide.services.httpClient.streamTimeoutInMillis", 30000l).longValue ()
			);
	}
	
}
