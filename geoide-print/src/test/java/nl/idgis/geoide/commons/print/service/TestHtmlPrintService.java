package nl.idgis.geoide.commons.print.service;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import javax.imageio.ImageIO;

import nl.idgis.geoide.commons.print.common.DocumentReference;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.documentcache.service.DefaultDocumentCache;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;
import nl.idgis.ogc.util.MimeContentType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;

public class TestHtmlPrintService {

	private ActorSystem actorSystem;
	private int count = 0;
	
	private StreamProcessor streamProcessor;
	private DocumentCache documentCache;
	private HtmlPrintService service;
	
	@Before
	public void before () {
		actorSystem = ActorSystem.create ();
		streamProcessor = new AkkaStreamProcessor (actorSystem);
		documentCache = DefaultDocumentCache.createInMemoryCache (actorSystem, streamProcessor, "geoide-print-" + (++ count), 10, 0.5, null);
		service = new HtmlPrintService (documentCache, streamProcessor, 1, 10000);
	}

	@After
	public void after () throws Exception {
		service.close ();
		((DefaultDocumentCache) documentCache).close ();
		((AkkaStreamProcessor) streamProcessor).close ();
		streamProcessor = null;
		JavaTestKit.shutdownActorSystem (actorSystem);
		actorSystem = null;
	}
	
	@Test
	public void testPrint () throws Throwable {
		store ("http://idgis.nl", "text/html", "<html><head></head><body><h1>Hello, World!</h1></body></html>");

		final Document document = print ("http://idgis.nl");
		
		assertEquals (new MimeContentType ("application/pdf"), document.getContentType ());
	}

	@Test
	public void testPrintWithObject () throws Throwable {
		store ("http://idgis.nl", "text/html", "<html><head></head><body><h1>Hello, World!</h1><object style=\"display: block; position: absolute; left: 0; top: 0; width: 100%; height: 100%;\" type=\"image/svg+xml\" data=\"http://idgis.nl/map.svg\"></object></html>");
		store ("http://idgis.nl/map.svg", "image/svg+xml", "<svg></svg>");

		final Document document = print ("http://idgis.nl");
		
		assertEquals (new MimeContentType ("application/pdf"), document.getContentType ());
	}
	
	@Test
	public void testPrintWithEscapedAttribute () throws Throwable {
		store ("http://idgis.nl", "text/html", "<html><head></head><body><h1 data-attr=\"&lt;&amp;&gt;\">Hello, World!</h1></body></html>");

		final Document document = print ("http://idgis.nl");
		
		assertEquals (new MimeContentType ("application/pdf"), document.getContentType ());
	}
	
	/**
	 * Tests whether references to related media (images) work when the URL contains an unescaped space.
	 * When the standard is strictly followed this is not allowed, however many HTML pages "in the wild"
	 * contain spaces in the source.
	 * 
	 * Relates to issue #32.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testPrintWithSpacesInAttribute () throws Throwable {
		store ("http://idgis.nl", "text/html", "<html><head></head><body><img src=\"http://idgis.nl/test image.png\"></body></html>");
		store ("http://idgis.nl/test%20image.png", "image/png", testPng ());
		
		final Document document = print ("http://idgis.nl");
		
		assertEquals (new MimeContentType ("application/pdf"), document.getContentType ());
	}
	
	private Document print (final String uri) throws Throwable {
		final PrintRequest printRequest = new PrintRequest (
				new DocumentReference (new MimeContentType ("text/html"), new URI (uri)), 
				new MimeContentType ("application/pdf"), 
				new URI ("http://idgis.nl")
			);
		
		return service.print (printRequest).get (30000);
	}
	
	private byte[] testPng () throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
		final BufferedImage img = new BufferedImage (10, 10, BufferedImage.TYPE_INT_ARGB);
		ImageIO.write (img, "PNG", bos);
		
		bos.close ();
		
		return bos.toByteArray ();
	}
	
	private void store (final String uri, final String contentType, final String content) throws Throwable {
		final ByteArrayOutputStream os = new ByteArrayOutputStream ();
		final PrintWriter writer = new PrintWriter (os);
		
		writer.write (content);
		
		writer.close ();
		os.close ();

		store (uri, contentType, os.toByteArray ());
	}
	
	private void store (final String uri, final String contentType, final byte[] data) throws Throwable {
		documentCache.store (new URI (uri), new MimeContentType (contentType), new ByteArrayInputStream (data)).get (10000);
	}
}
