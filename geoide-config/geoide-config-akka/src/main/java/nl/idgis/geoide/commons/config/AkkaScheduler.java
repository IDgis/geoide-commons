package nl.idgis.geoide.commons.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import akka.actor.ActorSystem;
import nl.idgis.geoide.util.GeoideScheduler;

@Component
public class AkkaScheduler implements GeoideScheduler {
	
	private final ActorSystem actorSystem;
	
	@Autowired
	public AkkaScheduler (final ActorSystem actorSystem) {
		this.actorSystem = actorSystem;
	}
	
	@Override
	public void waitForCompletion () {
		actorSystem.awaitTermination ();
	}
}
