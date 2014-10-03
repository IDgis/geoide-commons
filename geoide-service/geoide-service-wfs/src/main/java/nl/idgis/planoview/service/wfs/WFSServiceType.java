package nl.idgis.planoview.service.wfs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.idgis.planoview.commons.domain.ServiceIdentification;
import nl.idgis.planoview.service.FeatureServiceType;
import nl.idgis.planoview.service.ServiceType;
import nl.idgis.planoview.service.wfs.actors.WFS;
import akka.actor.ActorRef;
import akka.actor.Props;

public class WFSServiceType extends ServiceType implements FeatureServiceType {

	private final static Set<String> versions;
	static {
		final HashSet<String> v = new HashSet<> ();
		v.add ("1.1.0");
		versions = Collections.unmodifiableSet (v);
	}
	
	@Override
	public String getTypeName () {
		return "WFS";
	}

	@Override
	public Set<String> getSupportedVersions () {
		return Collections.unmodifiableSet (versions);
	}

	@Override
	public String normalizeEndpoint (final String endpoint) {
		final String endpointWithProtocol = endpoint.contains ("://") ? endpoint : "http://" + endpoint;
		
		if (endpointWithProtocol.contains ("?")) {
			if (!endpointWithProtocol.endsWith ("?")) {
				return endpointWithProtocol + "&";
			} else {
				return endpointWithProtocol;
			}
		} else {
			return endpointWithProtocol + "?";
		}
	}

	@Override
	public Props createServiceActorProps (final ActorRef serviceManager, final ServiceIdentification identification) {
		return WFS.mkProps (serviceManager, identification);
	}
}