package nl.idgis.geoide.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import akka.actor.ActorSystem;

/**
 * Component that manages the lifecycle of the Akka actorsystem. Implements the DisposableBean interface
 * to close the actorsystem when the application context is closed.
 */
@Component
public class AkkaLifecycle implements DisposableBean {

	private final static Logger log = LoggerFactory.getLogger (AkkaLifecycle.class);
	
	private final ActorSystem actorSystem;
	
	@Autowired
	public AkkaLifecycle (final ActorSystem actorSystem) {
		this.actorSystem = actorSystem;
	}

	/**
	 * Stops the akka actorsystem and awaits termination.
	 */
	@Override
	public void destroy () throws Exception {
		log.info ("Shutting down Akka actorsystem");
		actorSystem.shutdown ();
		actorSystem.awaitTermination ();
	}
}
