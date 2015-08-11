package nl.idgis.geoide.commons.report.template;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
		"<html><head></head><body></body></html>",
		"<html><head></head><body></body></html>"
	};
	
	@Before
	public void init () {
		documentStore = mock (DelegatingStore.class);
		fileStore = mock (FileStore.class);
		streamProcessor = mock (StreamProcessor.class);
		
		// Mock the document store:
		when (documentStore.fetch (any (URI.class))).then (invocation -> CompletableFuture.completedFuture (new Document () {
			@Override
			public URI getUri () throws URISyntaxException {
				return invocation.getArgumentAt (0, URI.class);
			}
			
			@Override
			public MimeContentType getContentType () {
				return new MimeContentType ("text/html");
			}
			
			@Override
			public Publisher<ByteString> getBody () {
				final String uri = invocation.getArgumentAt (0, URI.class).toString ();
				
				for (int i = 0; i < templateContent.length; ++ i) {
					if (uri.contains ("template-" + (i + 1))) {
						return new SingleValuePublisher<ByteString> (ByteStrings.fromArray (templateContent[i].getBytes ()));
					}
				}
				
				return new SingleValuePublisher<ByteString> (ByteStrings.empty ());
			}
		}));
		
		// Mock the fileStore:
		when (fileStore.getDirectories ()).then (invocation -> new File[] {
			new File ("template-1"),
			new File ("template-2"),
			new File ("template-3"),
			new File ("template-3")
		});
		
		// Mock the stream processor:
		when (streamProcessor.asInputStream (any (), anyLong ())).then (invocation -> {
			@SuppressWarnings("unchecked")
			final Publisher<ByteString> publisher = (Publisher<ByteString>)invocation.getArgumentAt (0, Publisher.class);
			final ByteString byteString = extractByteString (publisher);
			
			return new ByteArrayInputStream (byteString.toArray ());
		});
		
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
	public void testGetTemplateProperties () throws Throwable {
	}
	
	@Test
	public void testGetTemplates () throws Throwable {
		final ExternalizableJsonNode node = provider.getTemplates ().get ();
		
		assertNotNull (node);
		
		final JsonNode templates = node.getJsonNode ();
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

}
