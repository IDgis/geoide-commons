package nl.idgis.geoide.commons.print.service;

import nl.idgis.geoide.commons.print.common.Capabilities;
import nl.idgis.geoide.commons.print.common.Document;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import play.libs.F.Promise;
import akka.actor.ActorSystem;

/**
 * Print service implementation that converts HTML + several linked media to
 * PDF.
 */
public class HtmlPrintService implements PrintService {

	private final ActorSystem actorSystem;

	public HtmlPrintService (final ActorSystem actorSystem) {
		if (actorSystem == null) {
			throw new NullPointerException ("actorSystem cannot be null");
		}
		
		this.actorSystem = actorSystem;
	}
	
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
