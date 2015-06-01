package nl.idgis.geoide.commons.config;

import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.service.actors.ServiceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.libs.ws.WSClient;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Configuration
public class ServiceManagerConfig {

	private final static Logger log = LoggerFactory.getLogger (ServiceManagerConfig.class);
	
	@Bean
	@Autowired
	public ActorRef serviceManagerActor (final ActorSystem actorSystem, final ServiceTypeRegistry serviceTypeRegistry, final WSClient wsClient) {
		final ActorRef actor = actorSystem.actorOf (ServiceManager.mkProps (serviceTypeRegistry, wsClient), "serviceManager");
		log.info ("Created service manager actor: " + actor.toString ());
		return actor;
	}
}
