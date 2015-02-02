package nl.idgis.geoide.commons.print.service;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Static helper methods that are used to clean up a HTML document parsed by Jsoup.
 * 
 */
public final class HtmlCleanup {

	/**
	 * Performs all cleanup operations on the document:
	 * - Cleans up href attributes
	 * - Cleans up image src attributes.
	 * 
	 * @param document The document to clean.
	 */
	public static void cleanup (final Document document) {
		cleanupHrefs (document);
		cleanupImageSource (document);
	}
	
	/**
	 * Cleans up all "href" attributes in the document, removing the following quirks:
	 * - Spaces are replaced by %20.
	 * 
	 * @param document The document to cleanup.
	 */
	public static void cleanupHrefs (final Document document) {
		for (final Element element: document.select ("*[href]")) {
			element.attr ("href", cleanupHref (element.attr ("href")));
		}
	}
	
	/**
	 * Cleans up all "src" attributes in the document, removing the following quirks:
	 * - Spaces are replaced by %20.
	 * 
	 * @param document The document to cleanup.
	 */
	public static void cleanupImageSource (final Document document) {
		for (final Element element: document.select ("img[src]")) {
			element.attr ("src", cleanupHref (element.attr ("src")));
		}
	}
	
	/**
	 * Cleans up all "data" attributes in objects in the document, removing the following quirks:
	 * - Spaces are replaced by %20.
	 * 
	 * @param document The document to cleanup.
	 */
	public static void cleanupObjectData (final Document document) {
		for (final Element element: document.select ("object[data")) {
			element.attr ("data", cleanupHref (element.attr ("data")));
		}
	}
	
	private static String cleanupHref (final String input) {
		return input.replaceAll (" ", "%20");
	}
}
