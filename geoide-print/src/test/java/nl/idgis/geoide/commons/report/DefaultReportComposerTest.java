package nl.idgis.geoide.commons.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;
import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.print.PrintEvent;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;
import nl.idgis.geoide.commons.report.template.HtmlTemplateDocumentProvider;
import nl.idgis.geoide.map.DefaultMapView;
import nl.idgis.geoide.util.streams.PublisherReference;
import nl.idgis.geoide.util.streams.SingleValuePublisher;
import nl.idgis.geoide.util.streams.StreamProcessor;

public class DefaultReportComposerTest {

	private StreamProcessor streamProcessor;
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
	
	@SuppressWarnings("unchecked")
	@Before
	public void init () throws Throwable {
		MockitoAnnotations.initMocks (this);
		
		postProcessor = mock (ReportPostProcessor.class);
		templateProvider = mock (HtmlTemplateDocumentProvider.class);
		mapView = mock (DefaultMapView.class);
		documentCache = mock (DocumentCache.class);
		streamProcessor = mock (StreamProcessor.class);
		
		// Create mock templates:
		mockTemplates ();
		
		// Mock the stream processor:
		when (streamProcessor.createPublisherReference (any (), anyLong ()))
			.then (invocation -> new StaticPublisherReference<> (invocation.getArgumentAt (0, Publisher.class)));
		when (streamProcessor.resolvePublisherReference (any (), anyLong ()))
			.then (invocation -> invocation.getArgumentAt (0, StaticPublisherReference.class).getPublisher ());
		
		// Mock the post processor:
		when (postProcessor.process (any (TemplateDocument.class), any (org.jsoup.nodes.Document.class), any (ReportData.class)))
			.then (invocation -> CompletableFuture.completedFuture (new SingleValuePublisher<PrintEvent> (new PrintEvent (new Document(
						new URI ("generated://" + invocation.getArgumentAt (0, TemplateDocument.class).getTemplate ()),
						new MimeContentType ("application/pdf"),
						new StaticPublisherReference<> (new SingleValuePublisher<ByteString> (ByteStrings.fromArray (new byte[] { })))
					)
			))));
		
		composer = new DefaultReportComposer (postProcessor, templateProvider, mapView, documentCache);
	}
	
	@Test
	public void testEmptyTemplate () throws Throwable {
		print ("empty-template");
	}
	
	@Test
	public void testTemplateWithText () throws Throwable {
		final ObjectNode block = JsonFactory.mapper ().createObjectNode ();
		
		block.put ("id", "title");
		block.put ("text", "Hello, World!");
		block.put ("type", "text");
		
		print ("template-with-text-block", block);
		
		assertEquals ("Hello, World!", document.getElementById ("title").text ());
	}

	@Test
	public void testTemplateWithTextNoValue () throws Throwable {
		print ("template-with-text-block");
		
		assertEquals ("", document.getElementById ("title").text ());
	}
	
	@Test
	public void testTemplateWithTextMissingValue () throws Throwable {
		final ObjectNode block = JsonFactory.mapper ().createObjectNode ();
		
		block.put ("id", "title");
		
		print ("template-with-text-block", block);
		
		assertEquals ("", document.getElementById ("title").text ());
	}
	
	private Document print (final String template, final JsonNode ... blocks) throws Throwable {
		final ObjectNode node = JsonFactory.mapper ().createObjectNode ();
		final ObjectNode templateNode = node.putObject ("template");
		final ArrayNode blocksNode = templateNode.putArray ("blocks");
		
		for (final JsonNode blockNode: blocks) {
			blocksNode.add (blockNode);
		}
		
		templateNode.put ("id", template);
		
		final Document result = doCompose (JsonFactory.externalize (node));
		
		verify (postProcessor).process (templateDocumentCaptor.capture (), documentCaptor.capture (), reportDataCaptor.capture ());

		templateDocument = templateDocumentCaptor.getValue ();
		document = documentCaptor.getValue ();
		reportData = reportDataCaptor.getValue ();
		
		assertNotNull (templateDocument);
		assertNotNull (document);
		assertNotNull (reportData);
		
		return result;
	}
	
	private Document doCompose (final ExternalizableJsonNode clientInfo) throws Throwable {
		final List<PrintEvent> events = StreamProcessor.asList (composer.compose (clientInfo).get (30, TimeUnit.SECONDS)).get (30, TimeUnit.SECONDS);
		
		assertTrue (!events.isEmpty ());
		assertTrue (events.get (events.size () - 1).getEventType ().equals (PrintEvent.EventType.COMPLETE));
		
		return events.get (events.size () - 1).getDocument ().get ();
	}
	
	private void mockTemplates () throws Throwable {
		when (templateProvider.getTemplateDocument ("empty-template"))
			.then (invocation -> template ("empty-template", "<html><head></head><body></body></html>"));
		
		when (templateProvider.getTemplateDocument ("template-with-text-block"))
			.then (invocation -> template ("template-with-text-block", "<html><head></head><body><div class=\"block text\" id=\"title\"></div></body></html>"));
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
	
	private final static class StaticPublisherReference<T> implements PublisherReference<T> {
		private static final long serialVersionUID = 8177248641720307658L;
		
		private final Publisher<T> publisher;
		
		public StaticPublisherReference (final Publisher<T> publisher) {
			this.publisher = publisher;
		}
		
		public Publisher<T> getPublisher () {
			return publisher;
		}
	}
}
