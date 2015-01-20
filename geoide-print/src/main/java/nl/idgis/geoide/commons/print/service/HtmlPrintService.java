package nl.idgis.geoide.commons.print.service;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nl.idgis.geoide.commons.print.common.Capabilities;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.commons.print.svg.ChainedReplacedElementFactory;
import nl.idgis.geoide.commons.print.svg.SVGReplacedElementFactory;
import nl.idgis.geoide.documentcache.CachedDocument;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.ogc.util.MimeContentType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Node;
import org.w3c.tidy.Tidy;
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
	private final long cacheTimeoutMillis;
	private final ThreadPoolExecutor executor;
	private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<> ();

	public HtmlPrintService (final DocumentCache documentCache, final int maxThreads, final long cacheTimeoutMillis) {
		if (documentCache == null) {
			throw new NullPointerException ("documentCache cannot be null");
		}
		if (maxThreads < 1) {
			throw new IllegalArgumentException ("maxThreads should be >= 1");
		}

		this.documentCache = documentCache;
		this.cacheTimeoutMillis = cacheTimeoutMillis;
		this.executor = new ThreadPoolExecutor (1, maxThreads, 10, TimeUnit.SECONDS, workQueue);
	}

	@Override
	public void close () {
		executor.shutdownNow ();
	}
	
	@Override
	public Promise<CachedDocument> print (final PrintRequest printRequest) {
		if (!"text".equals (printRequest.getInputDocument().getContentType ().type ()) 
				|| !"html".equals (printRequest.getInputDocument().getContentType ().subType ())
				|| !"application".equals (printRequest.getOutputFormat ().type ())
				|| !"pdf".equals (printRequest.getOutputFormat ().subType ())) {
			return Promise.throwing (new PrintException.UnsupportedFormat (printRequest.getInputDocument ().getContentType (), printRequest.getOutputFormat ()));
		}
		
        final scala.concurrent.Promise<CachedDocument> scalaPromise = scala.concurrent.Promise$.MODULE$.<CachedDocument>apply ();

        executor.execute (new Runnable () {
			@Override
			public void run () {
				try {
					// Load the input document:
					final CachedDocument cachedDocument = documentCache.fetch (printRequest.getInputDocument ().getUri ()).get (cacheTimeoutMillis);
					
					// Load the HTML and convert to an XML document:
					final String xmlDocument;
					final String baseUrl = printRequest.getBaseUri () != null ? printRequest.getBaseUri ().toString () : cachedDocument.getUri ().toString ();
					final String inputEncoding = printRequest.getInputDocument ().getContentType ().parameters ().get ("charset");
					try (final InputStream htmlStream = cachedDocument.asInputStream ()) {
						
						final StringWriter writer = new StringWriter ();
						
						final Tidy tidy = new Tidy ();
						tidy.setXHTML (true);
						tidy.parse (htmlStream, writer);
						
						writer.close ();
						
						xmlDocument = writer.toString ();
						
						/*
						final Document document = Jsoup.parse (htmlStream, inputEncoding != null ? inputEncoding : "UTF-8", baseUrl);
						
						final OutputSettings outputSettings = new OutputSettings ();
						outputSettings.charset ("UTF-8");
						outputSettings.syntax (Syntax.xml);
						outputSettings.escapeMode(EscapeMode.xhtml);
						document.outputSettings (outputSettings);
					
						// Remove existing doctype declaration (if any):
						for (final Node node: document.childNodes ()) {
							if (node instanceof DocumentType) {
								node.remove ();
								break;
							}
						}
						
						// Insert a new XHTML doctype:
						final DocumentType docType = new DocumentType ("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "");
						document.childNode (0).before (docType);
						
						xmlDocument = document.toString ();
						*/
					}
					
					System.out.println (xmlDocument);
					
					// Generate PDF:
					final ChainedReplacedElementFactory cef = new ChainedReplacedElementFactory ();
					final ByteArrayOutputStream os = new ByteArrayOutputStream ();
					
					final ITextRenderer renderer = new ITextRenderer ();
					final ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent (renderer.getOutputDevice(), documentCache, cacheTimeoutMillis);
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
					final CachedDocument resultDocument = documentCache.store (new URI ("generated://" + UUID.randomUUID ().toString () + ".pdf"), new MimeContentType ("application/pdf"), os.toByteArray ()).get (cacheTimeoutMillis);
					
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
	
	 private static class ResourceLoaderUserAgent extends ITextUserAgent {
		 private final DocumentCache cache;
		 private final long timeout;
		 
		 public ResourceLoaderUserAgent (final ITextOutputDevice outputDevice, final DocumentCache cache, final long timeout) {
			 super (outputDevice);
			 
			 this.cache = cache;
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
				 final CachedDocument document = cache.fetch (new URI (uri)).get (timeout);
				 return document.asInputStream ();
			 } catch (URISyntaxException | DocumentCacheException e) {
				 // Fall back to the default user agent for other requests:
				 return super.resolveAndOpenStream (uri);
			 }
		 }
	 }	
}
