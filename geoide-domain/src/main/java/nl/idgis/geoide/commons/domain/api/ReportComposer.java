package nl.idgis.geoide.commons.domain.api;

import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.print.PrintEvent;

public interface ReportComposer {

	/**
	 * Composes a report for printing and sends the composed report to the postprocessor.
	 * 
	 * @param clientInfo 	client information in the form of a Json Node. 
	 * @return a promise of a report document (html)
	 */
	CompletableFuture<Publisher<PrintEvent>> compose (ExternalizableJsonNode clientInfo, String token) throws Throwable;

}