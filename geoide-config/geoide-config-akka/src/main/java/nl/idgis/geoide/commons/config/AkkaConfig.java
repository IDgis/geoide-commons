package nl.idgis.geoide.commons.config;

import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorSystem;

import com.typesafe.config.Config;

/**
 * Provides implementations of components that use an Akka actorsystem as backing.
 */
@Configuration
public class AkkaConfig {

	/**
	 * Creates an actorsystem based on the configuration under "geoide.service.akka".
	 * 
	 * @param config	The application main configuration.
	 */
	@Bean
	@Autowired
	public ActorSystem actorSystem (final Config config) {
		return ActorSystem.create ("service", config.getConfig ("geoide.service"));
	}
	
	/**
	 * Creates a stream processor that uses an Akka actorSystem for scheduling.
	 * 
	 * @param actorSystem	The Akka actorsystem to use for scheduling
	 */
	@Bean
	@Qualifier ("streamProcessor")
	@Autowired
	public StreamProcessor streamProcessor (final ActorSystem actorSystem) {
		return new AkkaStreamProcessor (actorSystem);
	}
}
