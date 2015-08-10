package nl.idgis.geoide.commons.report;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Publisher;

import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;
import nl.idgis.geoide.commons.report.template.HtmlTemplateDocumentProvider;
import nl.idgis.geoide.map.DefaultMapView;
import nl.idgis.geoide.util.streams.SingleValuePublisher;

public class DefaultReportComposerTest {

	private ReportPostProcessor postProcessor;
	private HtmlTemplateDocumentProvider templateProvider;
	private DefaultMapView mapView;
	private DocumentCache documentCache;
	private DefaultReportComposer composer;

	private @Captor ArgumentCaptor<TemplateDocument> templateDocumentCaptor;
	private @Captor ArgumentCaptor<org.jsoup.nodes.Document> documentCaptor;
	private @Captor ArgumentCaptor<ReportData> reportDataCaptor;
	
	private TemplateDocument templateDocument;
	private org.jsoup.nodes.Document document;
	private ReportData reportData;
	
	@Before
	public void init () throws Throwable {
		MockitoAnnotations.initMocks (this);
		
		postProcessor = mock (ReportPostProcessor.class);
		templateProvider = mock (HtmlTemplateDocumentProvider.class);
		mapView = mock (DefaultMapView.class);
		documentCache = mock (DocumentCache.class);
		
		// Create mock templates:
		mockTemplates ();
		
		// Mock the post processor:
		when (postProcessor.process (any (TemplateDocument.class), any (org.jsoup.nodes.Document.class), any (ReportData.class)))
			.then (invocation -> CompletableFuture.completedFuture (new Document() {
				@Override
				public URI getUri () throws URISyntaxException {
					return new URI ("generated://" + invocation.getArgumentAt (0, TemplateDocument.class).getTemplate ());
				}
				
				@Override
				public MimeContentType getContentType () {
					return new MimeContentType ("application/pdf");
				}
				
				@Override
				public Publisher<ByteString> getBody () {
					return new SingleValuePublisher<ByteString> (ByteStrings.fromArray (new byte[] { }));
				}
			}));
		
		composer = new DefaultReportComposer (postProcessor, templateProvider, mapView, documentCache);
	}
	
	@Test
	public void testEmptyTemplate () throws Throwable {
		print ("empty-template");
	}

	private Document print (final String template) throws Throwable {
		final ObjectNode node = JsonFactory.mapper ().createObjectNode ();
		final ObjectNode templateNode = node.putObject ("template");
		
		templateNode.put ("id", template);
		
		final Document result = composer.compose (JsonFactory.externalize (node)).get ();
		
		verify (postProcessor).process (templateDocumentCaptor.capture (), documentCaptor.capture (), reportDataCaptor.capture ());

		templateDocument = templateDocumentCaptor.getValue ();
		document = documentCaptor.getValue ();
		reportData = reportDataCaptor.getValue ();
		
		assertNotNull (templateDocument);
		assertNotNull (document);
		assertNotNull (reportData);
		
		return result;
	}
	
	private void mockTemplates () throws Throwable {
		when (templateProvider.getTemplateDocument ("empty-template"))
			.then (invocation -> template ("empty-template", "<html><head></head><body></body></html>"));
	}
	
	private CompletableFuture<TemplateDocument> template (final String templateName, final String content) throws Throwable {
		final TemplateDocument document = TemplateDocument
			.build ()
			.setDocumentUri (new URI ("template://" + templateName))
			.setContent (content)
			.setPageFormat ("A4")
			.setPageOrientation ("portrait")
			.create ();
		
		return CompletableFuture.completedFuture (document);
	}
}
