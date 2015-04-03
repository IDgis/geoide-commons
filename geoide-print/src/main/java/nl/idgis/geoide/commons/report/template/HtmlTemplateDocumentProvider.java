package nl.idgis.geoide.commons.report.template;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;





import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.service.DelegatingStore;
import nl.idgis.geoide.documentcache.service.FileStore;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.libs.F.Promise;
import play.libs.F.Tuple;
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
				final ObjectNode allTemplates =  JsonFactory.mapper ().createObjectNode();
				final ArrayNode templateList = allTemplates.arrayNode();
				for (final JsonNode template: templates) {
					templateList.add(template);	
				}
				
				return templateList;
			});
	}
	

	

	@Override
	public Promise<JsonNode> getTemplateProperties(final String templateName, final Promise<TemplateDocument>  html) {
		return (Promise<JsonNode>) html.map ((d) -> {
			final ObjectNode template = JsonFactory.mapper ().createObjectNode ();
			template.put("template", templateName);
			template.put ("description", d.getDocument().select("meta[name=description]").first().attr("content"));
			final ObjectNode propertiesNode =  JsonFactory.mapper ().createObjectNode();
			//final ArrayNode properties = propertiesNode.arrayNode();
			Elements blocks = d.getBlocks();
			for ( int n = 0; n< blocks.size(); n++) {
				if(blocks.get(n).hasClass("text")) {
					propertiesNode.put("name", blocks.get(n).select("#id").toString())
								  .put("maxwidth", blocks.get(n).select("^max-width").toString())	
								  .put("default",blocks.get(n).html()); 	
				}
				template.put("variables", propertiesNode);
			}
			
			return (JsonNode)template;

		});
	}	

}
