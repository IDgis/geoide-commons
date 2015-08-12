package nl.idgis.geoide.commons.print.service;

import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;

import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.print.Capabilities;
import nl.idgis.geoide.commons.domain.print.PrintEvent;
import nl.idgis.geoide.commons.domain.print.PrintRequest;

/**
 * A print service that delegates print and capabilities requests to a remote service.
 * 
 * This class is experimental and currently shouldn't be used.
 */
public class RemotePrintService implements PrintService {

	@Override
	public CompletableFuture<Publisher<PrintEvent>> print (final PrintRequest printRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Capabilities> getCapabilities () {
		// TODO Auto-generated method stub
		return null;
	}
}
