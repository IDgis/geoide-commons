package nl.idgis.geoide.commons.report.template;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.service.DelegatingStore;
import nl.idgis.geoide.documentcache.service.FileStore;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.jsoup.select.Elements;

import play.libs.F.Promise;
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
	public Promise<TemplateDocument> getTemplateDocument(String templateName) {
		
		URI uri = null;
		
		try {
	    	uri = new URI("template:///" + templateName + "/report.html");
		 }  catch(URISyntaxException e) {	
		    return Promise.throwing(e);
		}
		
    	Promise<Document> doc = documentStore.fetch(uri);
		return doc.map ((d) -> {
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
			
			return new HtmlTemplateDocument(templateURI,content);
		});
		
	   

	
	}
	
	public Promise<JsonNode> getTemplates() {
		File[] templateDirectories = fileStore.getDirectories();
		final List<Promise<JsonNode>> promises = new ArrayList<> (templateDirectories.length);
		
		for (int n = 0; n < templateDirectories.length; n++) {		
			String name = templateDirectories[n].getName();
			name = templateDirectories[n].getName().substring(name.lastIndexOf("/") + 1);
			
			promises.add (getTemplateProperties(name, getTemplateDocument (name)));
			
		}
		
		return Promise
			.sequence(promises)
			.map ((templates) -> {
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
	public Promise<JsonNode> getTemplateProperties(final String templateName, final Promise<TemplateDocument>  html) {
		return (Promise<JsonNode>) html.map ((d) -> {
			final ObjectNode template = JsonFactory.mapper ().createObjectNode ();
			template.put("template", templateName);
			template.put ("description", d.getDocument().select("meta[name=description]").first().attr("content"));
			final ArrayNode properties = template.arrayNode();
			Elements blocks = d.getBlocks();
			for ( int n = 0; n< blocks.size(); n++) {
				final ObjectNode property =  JsonFactory.mapper ().createObjectNode();
				if(blocks.get(n).hasClass("text")) {
					property.put("name", blocks.get(n).id())
							.put("maxwidth", blocks.get(n).attr("data-max-width"))	
							.put("default",blocks.get(n).text()); 	
					properties.add(property);
				}
			}
			template.put("variables", properties);
			return (JsonNode)template;

		});
	}	

}
