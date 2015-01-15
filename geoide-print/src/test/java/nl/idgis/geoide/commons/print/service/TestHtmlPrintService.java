package nl.idgis.geoide.commons.print.service;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URI;

import nl.idgis.geoide.commons.print.common.DocumentReference;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.documentcache.CachedDocument;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.documentcache.service.DefaultDocumentCache;
import nl.idgis.ogc.util.MimeContentType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;

public class TestHtmlPrintService {

	private static ActorSystem actorSystem;
	private static int count = 0;
	
	private DocumentCache documentCache;
	private HtmlPrintService service;
	
	@BeforeClass
	public static void startup () {
		actorSystem = ActorSystem.create ();
		
	}
	
	@AfterClass
	public static void teardown () {
		JavaTestKit.shutdownActorSystem (actorSystem);
		actorSystem = null;
	}
	
	
	@Before
	public void before () {
		documentCache = DefaultDocumentCache.createInMemoryCache (actorSystem, "geoide-print-" + (++ count), 10, 0.5, null);
		service = new HtmlPrintService (documentCache, 1, 10000);
	}

	@After
	public void after () throws Exception {
		service.close ();
		((DefaultDocumentCache) documentCache).close ();
	}
	
	@Test
	public void testPrint () throws Throwable {
		store ("http://idgis.nl", "text/html", "<html><head></head><body><h1>Hello, World!</h1></html>");

		final CachedDocument document = print ("http://idgis.nl");
		
		assertEquals (new MimeContentType ("application/pdf"), document.getContentType ());
	}

	@Test
	public void testPrintWithObject () throws Throwable {
		store ("http://idgis.nl", "text/html", "<html><head></head><body><h1>Hello, World!</h1><object style=\"display: block; position: absolute; left: 0; top: 0; width: 100%; height: 100%;\" type=\"image/svg+xml\" data=\"http://idgis.nl/map.svg\"></object></html>");
		store ("http://idgis.nl/map.svg", "image/svg+xml", "<svg></svg>");

		final CachedDocument document = print ("http://idgis.nl");
		
		assertEquals (new MimeContentType ("application/pdf"), document.getContentType ());
	}
	
	private CachedDocument print (final String uri) throws Throwable {
		final PrintRequest printRequest = new PrintRequest (
				new DocumentReference (new MimeContentType ("text/html"), new URI (uri)), 
				new MimeContentType ("application/pdf"), 
				new URI ("http://idgis.nl")
			);
		
		return service.print (printRequest).get (30000);
	}
	
	private void store (final String uri, final String contentType, final String content) throws Throwable {
		final ByteArrayOutputStream os = new ByteArrayOutputStream ();
		final PrintWriter writer = new PrintWriter (os);
		
		writer.close ();
		os.close ();
		
		documentCache.store (new URI (uri), new MimeContentType (contentType), new ByteArrayInputStream (os.toByteArray ())).get (10000);
	}
}
