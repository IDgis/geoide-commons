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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextReplacedElementFactory;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import play.libs.F.Promise;

/**
 * Print service implementation that converts HTML + several linked media to
 * PDF. HTML documents and all related media are fetched from a cache, with
 * optional read through store. The cache can be pre-seeded with generated content,
 * or the content can be fetched from a remote location (or a combination).
 * 
 * The print service uses a private thread pool for performing print jobs. The
 * number of threads in the pool determines the number of simultaneous jobs, jobs
 * are queued until a worker thread becomes available. All print jobs are performed
 * asynchronously from the calling thread.
 */
public class HtmlPrintService implements PrintService, Closeable {

	private static Logger log = LoggerFactory.getLogger (HtmlPrintService.class);
	
	private final DocumentCache documentCache;
	private final StreamProcessor streamProcessor;
	private final long cacheTimeoutMillis;
	private final ThreadPoolExecutor executor;
	private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<> ();

	/**
	 * Constructs a new HtmlPrintService by providing the related components (document cache and stream processor) and
	 * required configuration.
	 *  
	 * @param documentCache			The cache to retrieve content from.
	 * @param streamProcessor		The stream processor to use when consuming streams (document body's).
	 * @param maxThreads			The maximum number of threads to use. Should be >= 1. This determines the number of simultaneous 
	 * 								print requests that can be processed.  
	 * @param cacheTimeoutMillis	The timeout in milliseconds to use when the cache or its readthrough store is accessed.
	 */
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

	/**
	 * Stops the executor and cancels any pending print jobs. Scheduling new print jobs after closing the print
	 * service will result in an exception being thrown (RejectedExecutionException).
	 */
	@Override
	public void close () {
		executor.shutdownNow ();
	}
	
	/**
	 * Performs a print request on this service. The request is scheduled on the thread pool and is executed when a worker thread
	 * is available and the request is at the front of the queue.
	 * 
	 * {@inheritDoc}
	 */
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
					log.debug ("Printing document: " + printRequest.getInputDocument ().getUri ().toString ());
					
					// Load the input document:
					final Document cachedDocument = documentCache.fetch (printRequest.getInputDocument ().getUri ()).get (cacheTimeoutMillis);
					
					// Load the HTML and convert to an XML document:
					final String xmlDocument;
					final String baseUrl = printRequest.getBaseUri () != null ? printRequest.getBaseUri ().toString () : cachedDocument.getUri ().toString ();
					final String charset = printRequest.getInputDocument ().getContentType ().parameters ().containsKey ("charset") ? printRequest.getInputDocument ().getContentType ().parameters ().get ("charset") : "UTF-8"; 
					try (final InputStream htmlStream = streamProcessor.asInputStream (cachedDocument.getBody (), cacheTimeoutMillis)) {
						final org.jsoup.nodes.Document document = Jsoup.parse (htmlStream, charset, baseUrl);

						cleanup (document);
						
						final StringWriter writer = new StringWriter ();
						
						writeDocument (document, writer);
						
						
						xmlDocument = writer.toString ();
					}
					
					log.trace ("XML document after cleanup");
					
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
					final URI resultUri = new URI ("generated://" + UUID.randomUUID ().toString () + ".pdf");
					log.debug ("Storing result for " + printRequest.getInputDocument ().getUri ().toString () + " as " + resultUri.toString ());
					final Document resultDocument = documentCache.store (resultUri, new MimeContentType ("application/pdf"), os.toByteArray ()).get (cacheTimeoutMillis);
					
					scalaPromise.success (resultDocument);
				} catch (Throwable t) {
					scalaPromise.failure (t);
				}
			}
		});
        
		return Promise.wrap (scalaPromise.future ());
	}

	/**
	 * The capabilities of the HtmlPrintService are static: it can only convert HTML to PDF and handle
	 * the following linked content types: jpeg, png, gif and svg.
	 * 
	 * {@inheritDoc}
	 */
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
		final XMLStreamWriter streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter (writer);
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
	
	/**
	 * Peforms cleanup on the the given document. Delegates to {@link HtmlCleanup#cleanup(org.jsoup.nodes.Document)}
	 * 
	 * @param document The document to clean up. The document is modified in place.
	 */
	private void cleanup (final org.jsoup.nodes.Document document) {
		HtmlCleanup.cleanup (document);
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
			 log.debug ("IN resolveAndOpenStream: " + uri.toString ());

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
				 log.warn ("Unable to resolve related document " + uri.toString () + ", falling back to default implementation");
				 return super.resolveAndOpenStream (uri);
			 }
		 }
	 }	
}
