package geoide.config;

import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.libs.Akka;

@Configuration
public class StreamConfig {

	@Bean
	@Qualifier ("streamProcessor")
	@Autowired
	public StreamProcessor streamProcessor () {
		return new AkkaStreamProcessor (Akka.system ());
	}
}
