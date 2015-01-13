package nl.idgis.geoide.commons.print.service;

import nl.idgis.geoide.commons.print.common.Capabilities;
import nl.idgis.geoide.commons.print.common.Document;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import play.libs.F.Promise;

/**
 * Interface for a print service. A print service processes print requests which instruct
 * the service to convert a given input document (e.g. HTML) to a given printable media type (e.g. PDF).
 * 
 * Calls to operations on a print service are asynchronous, they return promises that will resolve to the
 * actual return value in the future. Especially printing itself can be a time consuming process.
 */
public interface PrintService {
	
	/**
	 * Performs the given print request on this service. Print requests are processed in asynchronous manner.
	 * 
	 * @param printRequest The print request to execute.
	 * @return A promise that will resolve to the resulting document, or raise a {@link PrintException}.
	 */
	Promise<Document> print (PrintRequest printRequest);
	
	/**
	 * Returns the capabilities of the print service.
	 * 
	 * @return A promise that will resolve to the capabilities of the service, or raise a {@link PrintException}.
	 */
	Promise<Capabilities> getCapabilities ();
}