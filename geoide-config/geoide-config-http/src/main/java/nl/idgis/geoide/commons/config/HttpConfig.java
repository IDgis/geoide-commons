package nl.idgis.geoide.commons.config;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.service.DefaultHttpClient;
import nl.idgis.geoide.util.ConfigWrapper;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConfig {
	
	private final Logger log = LoggerFactory.getLogger (HttpConfig.class);

	/**
	 * Creates a default HTTP client for use by any component that needs to access remote HTTP
	 * resources. Configured using the settings in "geoide.service.components.httpClient".
	 * 
	 * @param streamProcessor	The StreamProcessor to use when processing HTTP streams.
	 * @param config			Application configuration.
	 * @return					The HttpClient component.
	 */
	@Bean
	@Autowired
	public HttpClient httpClient (final StreamProcessor streamProcessor, final ConfigWrapper config) {
		final int blockSize = config.getInt ("geoide.service.components.httpClient.streamBlockSizeInBytes", 2048);
		final long timeout = config.getLong ("geoide.service.components.httpClient.streamTimeoutInMillis", 30000l);
		
		log.info ("Creating default HTTP client. Block size: " + blockSize + " bytes, timeout: " + timeout + " ms");
		
		return new DefaultHttpClient (
				streamProcessor,
				blockSize,
				timeout
			);
	}
}
