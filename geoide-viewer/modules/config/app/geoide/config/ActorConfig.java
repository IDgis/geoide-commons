package geoide.config;

import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.service.actors.ServiceManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.libs.Akka;
import akka.actor.ActorRef;

@Configuration
public class ActorConfig {

	@Bean
	@Autowired
	public ActorRef serviceManagerActor (final ServiceTypeRegistry serviceTypeRegistry) {
		return Akka.system ().actorOf (ServiceManager.mkProps (serviceTypeRegistry), "serviceManager");
	}
}
