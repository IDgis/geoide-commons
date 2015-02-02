package nl.idgis.geoide.commons.print.service;

import nl.idgis.geoide.commons.print.common.Capabilities;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.documentcache.Document;
import play.libs.F.Promise;

/**
 * A print service that delegates print and capabilities requests to a remote service.
 * 
 * This class is experimental and currently shouldn't be used.
 */
public class RemotePrintService implements PrintService {

	@Override
	public Promise<Document> print (final PrintRequest printRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Promise<Capabilities> getCapabilities () {
		// TODO Auto-generated method stub
		return null;
	}
}
