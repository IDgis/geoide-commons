package nl.idgis.geoide.commons.report;

import java.net.URI;

import nl.idgis.geoide.commons.print.common.DocumentReference;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.commons.print.service.PrintService;
import nl.idgis.geoide.commons.report.template.TemplateDocument;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.ogc.util.MimeContentType;
import play.libs.F.Function;
import play.libs.F.Promise;

public class ReportPostProcessor {
	private final PrintService printService;
	private final DocumentCache documentCache;
		
	/**
	 * A postprocessor for report printing. Stores the final report html in the documentcache and sends a print request 
	 * to the printservice  
	 */

	
	/**
	 * Constructs a report postprocessor
	 * 
	 * @param documentCache			The cache to store the report html.
	 * @param printService			The print to send the print request to
	 */
	public ReportPostProcessor (PrintService printService, DocumentCache documentCache) {
		this.printService = printService;
		this.documentCache = documentCache;
	}
	

	
	public Promise<Document> process (TemplateDocument template) throws Throwable {
		
		System.out.println("++++++++ filled template: " + template.asString());
		final URI documentUri = new URI (template.getStoreUri());

		return documentCache
				.store(documentUri, new MimeContentType ("text/html"), template.asString().getBytes())
				.flatMap(new Function<Document, Promise<Document>> () {
					public Promise<Document> apply (final Document a) throws Throwable {
						return printService.print (
								new PrintRequest (
										new DocumentReference (new MimeContentType ("text/html"), documentUri), 
										new MimeContentType ("application/pdf"), 
										documentUri
								)
							);
				}});
	}

				

	
	
	
	
	
}
