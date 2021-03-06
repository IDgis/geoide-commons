package nl.idgis.geoide.commons.report.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.fasterxml.jackson.databind.JsonNode;

import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;
import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;
import nl.idgis.geoide.documentcache.service.DelegatingStore;
import nl.idgis.geoide.documentcache.service.FileStore;
import nl.idgis.geoide.util.streams.PublisherReference;
import nl.idgis.geoide.util.streams.SingleValuePublisher;
import nl.idgis.geoide.util.streams.StreamProcessor;

public class HtmlTemplateDocumentProviderTest {

	private DelegatingStore documentStore;
	private FileStore fileStore;
	private StreamProcessor streamProcessor;
	private HtmlTemplateDocumentProvider provider;
	
	private final static String[] templateContent = new String[] {
		"<html><head><meta name=\"description\" content=\"template-1 description\"></head><body></body></html>",
		"<html><head></head><body></body></html>",
		"<html data-pageformat=\"A4\" data-left-margin=\"10\" data-right-margin=\"10\" data-top-margin=\"10\" data-bottom-margin=\"10\" data-page-orientation=\"portrait\" data-gutter-h=\"2\" data-gutter-v=\"2\"><meta name=\"description\" content=\"template-3 description\"><head></head><body></body></html>",
		"<html data-pageformat=\"A4\" data-left-margin=\"10\" data-right-margin=\"10\" data-top-margin=\"10\" data-bottom-margin=\"10\" data-page-orientation=\"portrait\" data-gutter-h=\"2\" data-gutter-v=\"2\"><meta name=\"description\" content=\"template-4 description\"><head></head><body><div class=\"block text\" id=\"title\"></div></body></html>"
	};
	
	@SuppressWarnings("unchecked")
	@Before
	public void init () {
		documentStore = mock (DelegatingStore.class);
		fileStore = mock (FileStore.class);
		streamProcessor = mock (StreamProcessor.class);
		
		// Mock the document store:
		when (documentStore.fetch (any (URI.class))).then (invocation -> {
				final String uri = invocation.getArgumentAt (0, URI.class).toString ();
				
				for (int i = 0; i < templateContent.length; ++ i) {
					if (uri.contains ("template-" + (i + 1))) {
						return CompletableFuture.completedFuture (new Document (
								invocation.getArgumentAt (0, URI.class), 
								new MimeContentType ("text/html"), 
								streamProcessor.createPublisherReference (
										new SingleValuePublisher<ByteString> (ByteStrings.fromArray (templateContent[i].getBytes ())),
										5000
							)));
					}
				}
				
				final PublisherReference<ByteString> body = streamProcessor.createPublisherReference (
						new SingleValuePublisher<ByteString> (ByteStrings.empty ()),
						5000
					);
				
				return CompletableFuture.completedFuture (new Document (
						invocation.getArgumentAt (0, URI.class),
						new MimeContentType ("text/html"),
						body
					));
			});
		
		// Mock the fileStore:
		when (fileStore.getDirectories ()).then (invocation -> new File[] {
			new File ("template-1"),
			new File ("template-2"),
			new File ("template-3"),
			new File ("template-4")
		});
		
		// Mock the stream processor:
		when (streamProcessor.asInputStream (any (), anyLong ())).then (invocation -> {
			final Publisher<ByteString> publisher = (Publisher<ByteString>)invocation.getArgumentAt (0, Publisher.class);
			final ByteString byteString = extractByteString (publisher);
			
			return new ByteArrayInputStream (byteString.toArray ());
		});
		when (streamProcessor.createPublisherReference (any (), anyLong ()))
			.then (invocation -> new StaticPublisherReference<> (invocation.getArgumentAt (0, Publisher.class)));
		when (streamProcessor.resolvePublisherReference (any (), anyLong ()))
			.then (invocation -> invocation.getArgumentAt (0, StaticPublisherReference.class).getPublisher ());
		
		provider = new HtmlTemplateDocumentProvider (documentStore, fileStore, streamProcessor);
	}
	
	@Test
	public void testGetTemplateDocument () throws Throwable {
		final TemplateDocument template = provider.getTemplateDocument ("template-1").get ();
		
		assertNotNull (template);
		assertEquals ("template-1", template.getTemplate ());
		assertEquals ("template-1 description", template.getDescription ());
	}
	
	@Test
	public void testGetTemplateDocumentWithoutDescription () throws Throwable {
		final TemplateDocument template = provider.getTemplateDocument ("template-2").get ();
		
		assertNotNull (template);
		assertEquals ("template-2", template.getTemplate ());
		assertEquals ("", template.getDescription ());
	}
	
	@Test
	public void testGetTemplateDocumentWithProperties () throws Throwable {
		final TemplateDocument template = provider.getTemplateDocument ("template-3").get ();
		
		assertNotNull (template);
		assertEquals ("template-3", template.getTemplate ());
		assertEquals ("template-3 description", template.getDescription ());
		
		assertEquals ("A4", template.getPageFormat ());
		assertEquals (10, template.getLeftMargin (), .001);
		assertEquals (10, template.getRightMargin (), .001);
		assertEquals (10, template.getTopMargin (), .001);
		assertEquals (10, template.getBottomMargin (), .001);
		assertEquals ("portrait", template.getPageOrientation ());
		assertEquals (2, template.getGutterH (), .001);
		assertEquals (2, template.getGutterV (), .001);
		
		assertEquals (0, template.getVariables ().size ());
	}
	
	@Test
	public void testGetTemplateDocumentWithVariable () throws Throwable {
		final TemplateDocument template = provider.getTemplateDocument ("template-4").get ();
		
		assertNotNull (template);
		assertEquals ("template-4", template.getTemplate ());
		assertEquals ("template-4 description", template.getDescription ());
		
		assertEquals ("A4", template.getPageFormat ());
		assertEquals (10, template.getLeftMargin (), .001);
		assertEquals (10, template.getRightMargin (), .001);
		assertEquals (10, template.getTopMargin (), .001);
		assertEquals (10, template.getBottomMargin (), .001);
		assertEquals ("portrait", template.getPageOrientation ());
		assertEquals (2, template.getGutterH (), .001);
		assertEquals (2, template.getGutterV (), .001);
		
		assertEquals (1, template.getVariables ().size ());
		assertEquals ("", template.getVariables ().get (0).getDefaultValue ());
		assertEquals ("title", template.getVariables ().get (0).getName ());
		assertEquals (0, template.getVariables ().get (0).getMaxwidth ());
	}
	
	@Test
	public void testGetTemplateProperties () throws Throwable {
		final TemplateDocument template = provider.getTemplateDocument ("template-4").get ();
		final ExternalizableJsonNode node = provider.getTemplateProperties (template).get ();
		
		assertNotNull (node);
		
		final JsonNode props = node.getJsonNode ();

		assertTemplate4 (props);
	}
	
	@Test
	public void testGetTemplates () throws Throwable {
		final ExternalizableJsonNode node = provider.getTemplates ().get ();
		
		assertNotNull (node);
		
		final JsonNode templates = node.getJsonNode ();
		
		assertEquals (4, templates.path ("templates").size ());
		
		assertEquals ("template-1", templates.path ("templates").path (0).path ("template").asText ());
		assertEquals ("template-2", templates.path ("templates").path (1).path ("template").asText ());
		assertEquals ("template-3", templates.path ("templates").path (2).path ("template").asText ());
		assertEquals ("template-4", templates.path ("templates").path (3).path ("template").asText ());
		
		assertTemplate4 (templates.path ("templates").path (3));
	}
	
	private void assertTemplate4 (final JsonNode props) {
		assertEquals ("A4", props.path ("pageFormat").asText ());
		assertEquals (10, props.path ("leftMargin").asDouble (), .001);
		assertEquals (10, props.path ("rightMargin").asDouble (), .001);
		assertEquals (10, props.path ("topMargin").asDouble (), .001);
		assertEquals (10, props.path ("bottomMargin").asDouble (), .001);
		assertEquals ("portrait", props.path ("pageOrientation").asText ());
		assertEquals (2, props.path ("gutterH").asDouble (), .001);
		assertEquals (2, props.path ("gutterV").asDouble (), .001);
		
		assertEquals (1, props.path ("variables").size ());
		assertEquals ("", props.path ("variables").get (0).path ("defaultValue").asText ());
		assertEquals ("title", props.path ("variables").get (0).path ("name").asText ());
		assertEquals (0, props.path ("variables").get (0).path ("maxwidth").asInt ());
	}
	
	private ByteString extractByteString (final Publisher<ByteString> publisher) {
		final AtomicReference<ByteString> result = new AtomicReference<ByteString> (null);
		
		publisher.subscribe (new Subscriber<ByteString> () {
			@Override
			public void onComplete () {
			}

			@Override
			public void onError (Throwable t) {
			}

			@Override
			public void onNext (final ByteString byteString) {
				result.set (byteString);
			}

			@Override
			public void onSubscribe (final Subscription subscription) {
				subscription.request (1);
			}
		});
		
		assertNotNull (result.get ());
		
		return result.get ();
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
