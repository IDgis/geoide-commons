package nl.idgis.geoide.commons.print;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import nl.idgis.geoide.commons.print.svg.ChainedReplacedElementFactory;
import nl.idgis.geoide.commons.print.svg.SVGReplacedElementFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Node;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextReplacedElementFactory;
import org.xhtmlrenderer.pdf.ITextUserAgent;

public class HtmlTranscode {

	public static void main (final String[] args) throws Throwable {
		if (args.length < 1) {
			System.err.println ("Usage: htmltranscode [out.pdf]");
			System.exit (1);
			return;
		}
		
		final URL url;
		if (args.length == 2) {
			url = new URL (args[1]);
		} else {
			url = null;
		}

		final String xmlDocument;
		
		try (final InputStream htmlStream = url == null ? HtmlTranscode.class.getClassLoader ().getResourceAsStream ("nl/idgis/geoide/commons/print/a4-portrait.html") : url.openStream ()) {
			final Document document = Jsoup.parse (htmlStream, "UTF-8", url == null ? "http://www.idgis.nl" : url.toString ());
			
			final OutputSettings outputSettings = new OutputSettings ();
			outputSettings.charset ("UTF-8");
			outputSettings.syntax (Syntax.xml);
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
		}
		
		System.out.println (xmlDocument);
		
		// Create a factory for SVG:
		// http://www.samuelrossille.com/posts/2013-08-13-render-html-with-svg-to-pdf-with-flying-saucer.html
		final ChainedReplacedElementFactory cef = new ChainedReplacedElementFactory ();
		
		try (final OutputStream os = new FileOutputStream (args[0])) {
			final ITextRenderer renderer = new ITextRenderer ();
			final ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent (renderer.getOutputDevice());
			callback.setSharedContext (renderer.getSharedContext ());
			renderer.getSharedContext ().setUserAgentCallback (callback);
			
			// Add the replaced element factory:
			cef.addReplacedElementFactory (new ITextReplacedElementFactory (renderer.getOutputDevice ()));
			cef.addReplacedElementFactory (new SVGReplacedElementFactory ());
			renderer.getSharedContext ().setReplacedElementFactory (cef);
			
			// Optional: set screen media, otherwise the print style is used.
			renderer.getSharedContext ().setMedia("screen");

			renderer.setDocumentFromString (xmlDocument, url == null ? null : url.toString ());
			renderer.layout ();
			renderer.createPDF (os);
		}
	}
	
	 private static class ResourceLoaderUserAgent extends ITextUserAgent {
		 public ResourceLoaderUserAgent (final ITextOutputDevice outputDevice) {
			 super (outputDevice);
		 }
		 
		 protected InputStream resolveAndOpenStream (final String uri) {
			 System.out.println("IN resolveAndOpenStream() " + uri);
			 
			 if (uri.startsWith ("resource://")) {
				 return getClass ().getClassLoader ().getResourceAsStream (uri.substring ("resource://".length ()));
			 } else {
				 return super.resolveAndOpenStream (uri);
			 }
		 }
	 }	
}
