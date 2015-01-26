package nl.idgis.geoide.commons.print.service;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import nl.idgis.geoide.commons.print.common.Capabilities;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.commons.print.svg.ChainedReplacedElementFactory;
import nl.idgis.geoide.commons.print.svg.SVGReplacedElementFactory;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.util.streams.StreamProcessor;
import nl.idgis.ogc.util.MimeContentType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextReplacedElementFactory;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import play.libs.F.Promise;

/**
 * Print service implementation that converts HTML + several linked media to
 * PDF.
 */
public class HtmlPrintService implements PrintService, Closeable {

	private final DocumentCache documentCache;
	private final StreamProcessor streamProcessor;
	private final long cacheTimeoutMillis;
	private final ThreadPoolExecutor executor;
	private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<> ();

	public HtmlPrintService (final DocumentCache documentCache, final StreamProcessor streamProcessor, final int maxThreads, final long cacheTimeoutMillis) {
		if (documentCache == null) {
			throw new NullPointerException ("documentCache cannot be null");
		}
		if (streamProcessor == null) {
			throw new NullPointerException ("streamProcessor cannot be null");
		}
		if (maxThreads < 1) {
			throw new IllegalArgumentException ("maxThreads should be >= 1");
		}

		this.documentCache = documentCache;
		this.streamProcessor = streamProcessor;
		this.cacheTimeoutMillis = cacheTimeoutMillis;
		this.executor = new ThreadPoolExecutor (1, maxThreads, 10, TimeUnit.SECONDS, workQueue);
	}

	@Override
	public void close () {
		executor.shutdownNow ();
	}
	
	@Override
	public Promise<Document> print (final PrintRequest printRequest) {
		if (!"text".equals (printRequest.getInputDocument().getContentType ().type ()) 
				|| !"html".equals (printRequest.getInputDocument().getContentType ().subType ())
				|| !"application".equals (printRequest.getOutputFormat ().type ())
				|| !"pdf".equals (printRequest.getOutputFormat ().subType ())) {
			return Promise.throwing (new PrintException.UnsupportedFormat (printRequest.getInputDocument ().getContentType (), printRequest.getOutputFormat ()));
		}
		
        final scala.concurrent.Promise<Document> scalaPromise = scala.concurrent.Promise$.MODULE$.<Document>apply ();

        executor.execute (new Runnable () {
			@Override
			public void run () {
				try {
					// Load the input document:
					final Document cachedDocument = documentCache.fetch (printRequest.getInputDocument ().getUri ()).get (cacheTimeoutMillis);
					
					// Load the HTML and convert to an XML document:
					final String xmlDocument;
					final String baseUrl = printRequest.getBaseUri () != null ? printRequest.getBaseUri ().toString () : cachedDocument.getUri ().toString ();
					final String charset = printRequest.getInputDocument ().getContentType ().parameters ().containsKey ("charset") ? printRequest.getInputDocument ().getContentType ().parameters ().get ("charset") : "UTF-8"; 
					try (final InputStream htmlStream = streamProcessor.asInputStream (cachedDocument.getBody (), cacheTimeoutMillis)) {
						final org.jsoup.nodes.Document document = Jsoup.parse (htmlStream, charset, baseUrl);
						
						final StringWriter writer = new StringWriter ();
						
						writeDocument (document, writer);
						
						
						xmlDocument = writer.toString ();
					}
					
					System.out.println (xmlDocument);
					
					// Generate PDF:
					final ChainedReplacedElementFactory cef = new ChainedReplacedElementFactory ();
					final ByteArrayOutputStream os = new ByteArrayOutputStream ();
					
					final ITextRenderer renderer = new ITextRenderer ();
					final ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent (renderer.getOutputDevice(), documentCache, streamProcessor, cacheTimeoutMillis);
					callback.setSharedContext (renderer.getSharedContext ());
					renderer.getSharedContext ().setUserAgentCallback (callback);
					
					// Add the replaced element factory:
					cef.addReplacedElementFactory (new ITextReplacedElementFactory (renderer.getOutputDevice ()));
					cef.addReplacedElementFactory (new SVGReplacedElementFactory ());
					renderer.getSharedContext ().setReplacedElementFactory (cef);
					
					// Optional: set screen media, otherwise the print style is used.
					renderer.getSharedContext ().setMedia ("screen");

					renderer.setDocumentFromString (xmlDocument, baseUrl);
					renderer.layout ();
					renderer.createPDF (os);
					
					// Store the document in the cache:
					os.close ();
					final Document resultDocument = documentCache.store (new URI ("generated://" + UUID.randomUUID ().toString () + ".pdf"), new MimeContentType ("application/pdf"), os.toByteArray ()).get (cacheTimeoutMillis);
					
					scalaPromise.success (resultDocument);
				} catch (Throwable t) {
					scalaPromise.failure (t);
				}
			}
		});
        
		return Promise.wrap (scalaPromise.future ());
	}

	@Override
	public Promise<Capabilities> getCapabilities () {
		final List<MimeContentType> supportedLinkedMedia = new ArrayList<> ();
		
		supportedLinkedMedia.add (new MimeContentType ("image/jpeg"));
		supportedLinkedMedia.add (new MimeContentType ("image/png"));
		supportedLinkedMedia.add (new MimeContentType ("image/gif"));
		supportedLinkedMedia.add (new MimeContentType ("image/svg+xml"));
		
		final List<MimeContentType> outputFormats = new ArrayList<> ();
		
		outputFormats.add (new MimeContentType ("application/pdf"));
		
		final List<Capabilities.InputFormat> inputFormats = new ArrayList<> (1);
		
		inputFormats.add (new Capabilities.InputFormat (
				new MimeContentType ("text/html"), 
				supportedLinkedMedia, outputFormats
			));
		
		return Promise.pure (new Capabilities (
				inputFormats, 
				workQueue.size ()
			));
	}
	
	private void writeDocument (final org.jsoup.nodes.Document document, final Writer writer) throws XMLStreamException, FactoryConfigurationError {
		final XMLStreamWriter streamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter (writer);
		try {
			writeNode (document, streamWriter);
		} finally {
			streamWriter.close ();
		}
	}
	
	private void writeNode (final Node node, final XMLStreamWriter writer) throws XMLStreamException {
		if (node instanceof org.jsoup.nodes.Document) {
			for (final Node childNode: node.childNodes ()) {
				writeNode (childNode, writer);
			}
		} else if (node instanceof Element) {
			final Element element = (Element) node;
			
			// Write the element name:
			writer.writeStartElement (element.nodeName ());
			
			// Write attributes:
			for (final Attribute attribute: element.attributes ()) {
				writer.writeAttribute (attribute.getKey (), attribute.getValue ());
			}
			
			// Write children:
			for (final Node childNode: element.childNodes ()) {
				writeNode (childNode, writer);
			}
			
			// End the element:
			writer.writeEndElement ();
		} else if (node instanceof TextNode) {
			writer.writeCharacters (((TextNode) node).text ());
		} else if (node instanceof DataNode) {
			writer.writeCData (((DataNode) node).getWholeData ());
		}
	}
	
	
	 private static class ResourceLoaderUserAgent extends ITextUserAgent {
		 private final DocumentCache cache;
		 private final StreamProcessor streamProcessor;
		 private final long timeout;
		 
		 public ResourceLoaderUserAgent (final ITextOutputDevice outputDevice, final DocumentCache cache, final StreamProcessor streamProcessor, final long timeout) {
			 super (outputDevice);
			 
			 this.cache = cache;
			 this.streamProcessor = streamProcessor;
			 this.timeout = timeout;
		 }
		 
		 protected InputStream resolveAndOpenStream (final String uri) {
			 System.out.println("IN resolveAndOpenStream() " + uri);

			 // Serve resources from the classpath:
			 /*
			 if (uri.startsWith ("resource://")) {
				 return getClass ().getClassLoader ().getResourceAsStream (uri.substring ("resource://".length ()));
			 }
			 */

			 try {
				 // Attempt to load cached resources:
				 final Document document = cache.fetch (new URI (uri)).get (timeout);
				 return streamProcessor.asInputStream (document.getBody (), timeout);
			 } catch (URISyntaxException | DocumentCacheException e) {
				 // Fall back to the default user agent for other requests:
				 return super.resolveAndOpenStream (uri);
			 }
		 }
	 }	
}
