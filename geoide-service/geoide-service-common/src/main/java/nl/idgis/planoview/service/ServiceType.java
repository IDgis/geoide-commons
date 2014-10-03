package nl.idgis.planoview.service;

import java.util.Set;

import nl.idgis.planoview.commons.domain.ServiceIdentification;
import akka.actor.ActorRef;
import akka.actor.Props;

public abstract class ServiceType {

	public abstract String getTypeName ();
	public abstract Set<String> getSupportedVersions ();
	public abstract String normalizeEndpoint (String endpoint);
	
	public boolean isVersionSupported (final String version) {
		return getSupportedVersions ().contains (version);
	}
	
	public ServiceIdentification createIdentification (final String endpoint, final String version) {
		if (!isVersionSupported (version)) {
			throw new IllegalArgumentException (String.format ("Unsupported version %s for service type %s", version, getTypeName ()));
		}
		
		return new ServiceIdentification (getTypeName (), normalizeEndpoint (endpoint), version);
	}
	
	public abstract Props createServiceActorProps (ActorRef serviceManager, ServiceIdentification identification);
}
