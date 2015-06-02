package nl.idgis.geoide.commons.report.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.api.TemplateDocumentProvider;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;
import nl.idgis.geoide.documentcache.service.DelegatingStore;
import nl.idgis.geoide.documentcache.service.FileStore;
import nl.idgis.geoide.util.Futures;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class HtmlTemplateDocumentProvider implements TemplateDocumentProvider {
	private final DelegatingStore documentStore;
	private final FileStore fileStore;
	private final StreamProcessor streamProcessor;
	
	public HtmlTemplateDocumentProvider(DelegatingStore documentStore, FileStore fileStore, StreamProcessor streamProcessor) {
		this.documentStore = documentStore;
		this.fileStore = fileStore;
		this.streamProcessor = streamProcessor;
	}
	
	@Override
	public CompletableFuture<TemplateDocument> getTemplateDocument(String templateName) {
		
		URI uri;
		
		try {
	    	uri = new URI("template:///" + templateName + "/report.html");
		 }  catch(URISyntaxException e) {	
		    return Futures.throwing(e);
		}
		
    	CompletableFuture<Document> doc = documentStore.fetch(uri);
		return doc.thenApply ((d) -> {
			try {
				URI templateURI = new URI("stored://" + UUID.randomUUID ().toString ());
				
				final InputStream inputStream = streamProcessor.asInputStream (d.getBody (), 5000);
				
				final byte[] buffer = new byte[4096];
				ByteString data = ByteStrings.empty ();
				int nRead;
				while ((nRead = inputStream.read (buffer, 0, buffer.length)) >= 0) {
					data = data.concat (ByteStrings.fromArray (buffer, 0, nRead));
				}
				inputStream.close ();
						
				final String content = new String (data.toArray ());
				
				return createTemplateDocument (templateName, templateURI, content, uri);
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException (e);
			}
		});
	}
	
	private static TemplateDocument createTemplateDocument (final String templateName, final URI templateURI, final String htmlContent, final URI uri) {
		final org.jsoup.nodes.Document html = Jsoup.parse (htmlContent, templateURI.toString());
		
		final TemplateDocument.Builder builder = TemplateDocument 
			.build ()
			.setUri (uri)
			.setDocumentUri (templateURI)
			.setRightMargin (html.select("html").attr("data-right-margin").isEmpty() ? 20 : Double.parseDouble(html.select("html").attr("data-right-margin")))
			.setLeftMargin (html.select("html").attr("data-left-margin").isEmpty () ? 20 : Double.parseDouble(html.select("html").attr("data-left-margin")))
			.setTopMargin (html.select("html").attr("data-top-margin").isEmpty () ? 20 : Double.parseDouble(html.select("html").attr("data-top-margin")))
			.setBottomMargin (html.select("html").attr("data-bottom-margin").isEmpty () ? 20 : Double.parseDouble(html.select("html").attr("data-bottom-margin")))
			.setPageFormat (html.select("html").attr("data-pageformat").isEmpty () ? "A4" : html.select("html").attr("data-pageformat"))
			.setPageOrientation (html.select("html").attr("data-page-orientation").isEmpty () ? "portrait" : html.select("html").attr("data-page-orientation"))
			.setGutterH (html.select("html").attr("data-gutter-h").isEmpty () ? 2 : Double.parseDouble(html.select("html").attr("data-gutter-h")))
			.setGutterV (html.select("html").attr("data-gutter-v").isEmpty () ? 2 : Double.parseDouble(html.select("html").attr("data-gutter-v")))
			.setColCount (html.select("html").attr("data-col-count").isEmpty () ? 12 : Integer.parseInt(html.select("html").attr("data-col-count")))
			.setRowCount (html.select("html").attr("data-row-count").isEmpty () ? 12 : Integer.parseInt(html.select("html").attr("data-row-count")))
			.setTemplate (templateName)
			.setDescription (html.select("meta[name=description]").first().attr("content"));
		
		for (final Element block: html.getElementsByClass ("block")) {
			if(block.hasClass ("text")) {
				builder.addVariable (block.id (), block.text (), block.attr ("data-max-width").isEmpty () ? 0 : Integer.valueOf (block.attr ("data-max-width")));
			}
		}
		
		return builder.create ();
	}
	
	@Override
	public CompletableFuture<JsonNode> getTemplates() {
		File[] templateDirectories = fileStore.getDirectories();
		final List<CompletableFuture<JsonNode>> promises = new ArrayList<> (templateDirectories.length);
		
		for (int n = 0; n < templateDirectories.length; n++) {		
			String name = templateDirectories[n].getName();
			name = templateDirectories[n].getName().substring(name.lastIndexOf("/") + 1);
			
			promises.add (getTemplateDocument (name).thenCompose (this::getTemplateProperties));
		}
		
		return Futures
			.all(promises)
			.thenApply ((templates) -> {
				final ArrayNode templateList = JsonFactory.mapper ().createObjectNode().arrayNode();
				for (final JsonNode template: templates) {
					templateList.add(template);	
				}
				ObjectNode allTemplates = JsonFactory.mapper ().createObjectNode();
				allTemplates.put("templates", templateList);
				return allTemplates;
			});
	}
	
	@Override
	public CompletableFuture<JsonNode> getTemplateProperties(final TemplateDocument template) {
		return CompletableFuture.completedFuture (JsonFactory.externalize (JsonFactory.mapper ().valueToTree (template)));
	}	
}
