package nl.idgis.geoide.commons.report;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.print.PrintRequest;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;

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
	}
	
	/**
	 * Asserts that printing an empty document results in a request to the print service.
	 */
	@Test
	public void testProcessEmptyDocument () throws Throwable {
		final TemplateDocument template = TemplateDocument
				.build ()
				.create ();
		final Document htmlDocument = new Document ("http://www.idgis.nl");
		
		postProcessor.process (template, htmlDocument, reportData);
		
		verify (printService).print (requestCaptor.capture ());

		final PrintRequest request = requestCaptor.getValue ();
		
		assertNotNull (request);
		assertEquals ("http://www.idgis.nl", request.getBaseUri ());
		assertEquals ("application/pdf", request.getOutputFormat ().toString ());
	}
}
