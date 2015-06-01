package geoide.config;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.util.AkkaFutures;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.Play;
import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;

@Configuration
public class ActorConfig {

	@Bean
	@Autowired
	public ActorRef serviceManagerActor (final ServiceTypeRegistry serviceTypeRegistry) throws InterruptedException, ExecutionException, TimeoutException {
		final ActorSelection selection = Akka.system ().actorSelection (Play.application().configuration ().getString ("geoide.web.actors.serviceManager"));
		final ActorRef actorRef = AkkaFutures.asCompletableFuture (selection.resolveOne (new FiniteDuration (10000, TimeUnit.MILLISECONDS)), Akka.system ().dispatcher ()).get (10000, TimeUnit.MILLISECONDS);
		
		// return Akka.system ().actorOf (ServiceManager.mkProps (serviceTypeRegistry), "serviceManager");
		return actorRef;
	}
}
