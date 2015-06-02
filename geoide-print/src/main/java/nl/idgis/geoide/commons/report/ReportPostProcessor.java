package nl.idgis.geoide.commons.report;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.print.DocumentReference;
import nl.idgis.geoide.commons.domain.print.PrintRequest;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;

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
	

	
	/**
	 * start the post-processing of the report document, writes the resulting pdf to the documentstore
	 * 
	 * @param template		the report to print ( a "filled" template document)
	 * @return 				a pdf Document
	 */
	public CompletableFuture<Document> process (TemplateDocument template, ReportData reportData) throws Throwable {
		

		final URI documentUri = template.getDocumentUri();

		final Map<String, Object> layoutParameters = new HashMap<String, Object>();
		layoutParameters.put("grid-page-size",template.getPageFormat());
		layoutParameters.put("grid-page-orientation", template.getPageOrientation());
		layoutParameters.put("grid-page-width", reportData.getFormat().getWidth() + "mm");
		layoutParameters.put("grid-page-height", reportData.getFormat().getHeight() + "mm");
		layoutParameters.put("grid-margin-top", reportData.getTopMargin() + "mm");
		layoutParameters.put("grid-margin-bottom", reportData.getBottomMargin() + "mm");
		layoutParameters.put("grid-margin-left", reportData.getLeftMargin() + "mm");
		layoutParameters.put("grid-margin-right", reportData.getRightMargin() + "mm");
		layoutParameters.put("grid-gutter-h", reportData.getGutterH() + "mm");
		layoutParameters.put("grid-gutter-v", reportData.getGutterV() + "mm");
		layoutParameters.put("grid-row-count", reportData.getRowCount());
		layoutParameters.put("grid-column-count", reportData.getColCount());
		layoutParameters.put("grid-debug", false);
		
		
		
		return documentCache
				.store(documentUri, new MimeContentType ("text/html"), template.asString().getBytes())
				.thenCompose ((final Document a) -> {
						try {
							return printService.print (
									new PrintRequest (
											new DocumentReference (new MimeContentType ("text/html"), documentUri), 
											new MimeContentType ("application/pdf"), 
											makeBaseUri (template.getUri ()),
											layoutParameters
									)
								);
						} catch (Exception e) {
							throw new RuntimeException (e);
						}
				});
	}

	private URI makeBaseUri (final URI uri) throws URISyntaxException {
		final String path = uri.getPath ();
		final int n = path != null ? path.lastIndexOf ('/') : -1;

		return new URI (
				uri.getScheme (), 
				uri.getUserInfo (), 
				uri.getHost (), 
				uri.getPort (),
				n >= 0 ? path.substring (0, n + 1) : null,
				null, 
				null
			);
	}
}
