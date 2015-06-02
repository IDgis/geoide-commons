package geoide.config;

import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;
import nl.idgis.geoide.commons.remote.transport.AkkaTransport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.Play;
import play.libs.Akka;

@Configuration
public class RemoteConfig {

	@Bean
	public RemoteServiceFactory remoteServiceFactory () {
		return new RemoteServiceFactory ();
	}
	
	@Bean
	@Autowired
	public RemoteMethodClient remoteMethodClient () {
		final String remoteActorRef = Play.application ().configuration ().getString ("geoide.web.actors.remoteMethodServer", "akka.tcp://service@127.0.0.1:2552/user/remote-method-server");
		final String apiServerName = Play.application ().configuration ().getString ("geoide.web.remoteMethodClient.apiServerName", "api");
		final String actorName = Play.application ().configuration ().getString ("geoide.web.remoteMethodClient.actorName", "remote-method-client");
		final long timeout = Play.application ().configuration ().getLong ("geoide.web.remoteMethodClient.timeoutInMillis", 10000l);
		
		final AkkaTransport transport = new AkkaTransport (Akka.system (), actorName, timeout);
	
		return transport.connect (remoteActorRef, apiServerName);
	}
}
