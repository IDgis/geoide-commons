package nl.idgis.geoide.commons.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import akka.util.ByteString;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.print.PrintRequest;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;
import nl.idgis.geoide.util.streams.PublisherReference;

public class ReportPostProcessorTest {

	private PrintService printService;
	private DocumentCache documentCache;
	private ReportData reportData;
	private ReportPostProcessor postProcessor;
	@Captor private ArgumentCaptor<PrintRequest> requestCaptor;
	
	@Before
	public void createObjects () {
		MockitoAnnotations.initMocks (this);
		
		printService = mock (PrintService.class);
		documentCache = mock (DocumentCache.class);
		reportData = ReportData.build ().create ();
		postProcessor = new ReportPostProcessor (printService, documentCache);

		// Make the mocked document cache return the document immediately:
		when (documentCache.store (any (URI.class), any (MimeContentType.class), any (byte[].class))).then (invocation -> {
			return CompletableFuture.completedFuture (new nl.idgis.geoide.commons.domain.document.Document (
					invocation.getArgumentAt (0, URI.class),
					invocation.getArgumentAt (1, MimeContentType.class),
					new PublisherReference<ByteString> () {
						private static final long serialVersionUID = 4553437063579695720L; 
					}
				));
		});
		
		// Make the mocked print service return a value:
		when (printService.print (any (PrintRequest.class))).then (invocation -> {
			return CompletableFuture.completedFuture (new nl.idgis.geoide.commons.domain.document.Document (
					new URI ("generated://print-result"),
					new MimeContentType ("application/pdf"),
					new PublisherReference<ByteString> () {
						private static final long serialVersionUID = 6917787825571305360L;
					}
				));
		});
	}
	
	/**
	 * Asserts that printing an empty document results in a request to the print service.
	 */
	@Test
	public void testProcessEmptyDocument () throws Throwable {
		final TemplateDocument template = TemplateDocument
				.build ()
				.setUri (new URI ("http://www.idgis.nl"))
				.setDocumentUri (new URI ("http://www.idgis.nl"))
				.create ();
		final Document htmlDocument = new Document ("http://www.idgis.nl");
		
		postProcessor.process (template, htmlDocument, reportData).get ();
		
		verify (printService).print (requestCaptor.capture ());

		final PrintRequest request = requestCaptor.getValue ();
		
		assertNotNull (request);
		assertEquals (new URI ("http://www.idgis.nl"), request.getBaseUri ());
		assertEquals ("text/html", request.getInputDocument().getContentType ().toString ().split("\\;")[0]);
		assertEquals ("application/pdf", request.getOutputFormat ().toString ());
		
		assertEquals (template.getPageFormat(), request.getLayoutParameters ().get ("grid-page-size"));
		assertEquals (template.getPageOrientation(), request.getLayoutParameters ().get ("grid-page-orientation"));
		assertEquals (reportData.getFormat().getWidth() + "mm", request.getLayoutParameters ().get ("grid-page-width"));
		assertEquals (reportData.getFormat().getHeight() + "mm", request.getLayoutParameters ().get ("grid-page-height"));
		assertEquals (reportData.getTopMargin() + "mm", request.getLayoutParameters ().get ("grid-margin-top"));
		assertEquals (reportData.getBottomMargin() + "mm", request.getLayoutParameters ().get ("grid-margin-bottom"));
		assertEquals (reportData.getLeftMargin() + "mm", request.getLayoutParameters ().get ("grid-margin-left"));
		assertEquals (reportData.getRightMargin() + "mm", request.getLayoutParameters ().get ("grid-margin-right"));
		assertEquals (reportData.getGutterH() + "mm", request.getLayoutParameters ().get ("grid-gutter-h"));
		assertEquals (reportData.getGutterV() + "mm", request.getLayoutParameters ().get ("grid-gutter-v"));
		assertEquals (reportData.getRowCount(), request.getLayoutParameters ().get ("grid-row-count"));
		assertEquals (reportData.getColCount (), request.getLayoutParameters ().get ("grid-column-count"));
		assertEquals (false, request.getLayoutParameters ().get ("grid-debug"));
	}
}
