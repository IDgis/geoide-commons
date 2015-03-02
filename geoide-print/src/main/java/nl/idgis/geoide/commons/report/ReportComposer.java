package nl.idgis.geoide.commons.report;


import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.report.blocks.MapBlockComposer;
import nl.idgis.geoide.commons.report.blocks.TextBlockComposer;
import nl.idgis.geoide.commons.report.template.TemplateDocument;
import nl.idgis.geoide.commons.report.template.TemplateDocumentProvider;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.map.MapView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * A component that combines information from the client (p.e. viewerstate) with a template to compose 
 * a report for printing.
 * 
 * This component holds several specialized composers that are responsible for their own task (p.e. mapcomposer)
 */

public class ReportComposer {
	private final ReportPostProcessor processor;
	private final TemplateDocumentProvider templateProvider;
	private TextBlockComposer textBlockComposer;
	private MapBlockComposer mapBlockComposer;
	
	
	/**
	 * Constructs a report composer with a series of specialized copomsers
	 * 
	 * @param postprocessor 	the postprocessor to send the report to, before printing
	 * @param templateprovider 	the template provider to retrieve the report template from. 
	 */
	
	public ReportComposer (ReportPostProcessor processor, TemplateDocumentProvider templateProvider, MapView mapView, DocumentCache documentCache) {
		this.processor =  processor;
		this.templateProvider = templateProvider;
		this.textBlockComposer = new TextBlockComposer();
		this.mapBlockComposer = new MapBlockComposer(mapView, documentCache);	
	}
	
	/**
	 * Composes a report for printing and sends the composed report to the postprocessor.
	 * 
	 * @param reportInfo 	client information in the form of a Json Node. 
	 */
	public Promise<Document> compose (JsonNode reportInfo) throws Throwable {
		
		//parse templateInfo
		final JsonNode viewerStates = reportInfo.findPath("viewerstates");
		final JsonNode templateInfo = reportInfo.findPath("template");
		final JsonNode blocks = templateInfo.path("blocks");
		//TODO get template path from configuration 
		final String templatePath = "templates/";
		final String templateUrl = templatePath + templateInfo.path ("id").asText() + ".html";
		
		
		final Map<String, JsonNode> viewerStateNodes = new HashMap<> ();
		for (final JsonNode stateNode: viewerStates) {
			final JsonNode stateId =  stateNode.path ("id");
			viewerStateNodes.put(stateId.textValue(), stateNode);
		}

		
		TemplateDocument template = templateProvider.getTemplateDocument(templateUrl);
		PaperFormat format = PaperFormat.valueOf(template.select("html").attr("data-pageformat") + template.select("html").attr("data-page-orientation"));
		ReportData reportData = new ReportData(format,Integer.parseInt(template.select("html").attr(("data-left-margin"))),
												Integer.parseInt(template.select("html").attr("data-right-margin")));

		
		ObjectMapper mapper = new ObjectMapper();
		
		Elements blockElements = template.body().select(".block");

		
		for (Element blockElement : blockElements) {
			//special block date
			if(blockElement.id().equals("date")) {
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Date date = new Date();
				ObjectNode blockNode = mapper.createObjectNode();
				blockNode.put ("tag", "p");
				blockNode.put ("text", dateFormat.format(date));
				textBlockComposer.compose(blockNode, blockElement, reportData);
			}
			
			//specialblock scale
			if(blockElement.id().equals("scale")) {
				String viewerStateId = blockElement.attr("data-viewerstate-id");
				ObjectNode blockNode = mapper.createObjectNode();
				blockNode.put ("tag", "p");
				blockNode.put ("text", "1 : " + viewerStateNodes.get(viewerStateId).get("scale"));
				textBlockComposer.compose(blockNode, blockElement, reportData);
			}
			
			for (final JsonNode blockNode: blocks) {
				final String blockId = blockNode.path("id").asText().toLowerCase();
				if(blockElement.id().equals(blockId)){
					final JsonNode blockType = blockNode.path("type");
					if(	blockType.textValue().equals("text")) {
						textBlockComposer.compose(blockNode, blockElement, reportData);
					}
					if(	blockType.textValue().equals("map")) {
						final JsonNode stateId = blockNode.path("viewerstate");
						
						ObjectNode node = mapper.createObjectNode();
						node.set("info", blockNode);
						node.put("viewerstate", viewerStateNodes.get(stateId.textValue()));
						
						mapBlockComposer.compose(node, blockElement, reportData);
						URI blockUri = 	mapBlockComposer.getBlockCssUri();
						
						Element linkElement = template.head().appendElement("link"); 
						linkElement.attr("rel", "stylesheet");
						linkElement.attr("href", blockUri.toString());
						
					}
					
				};
				
			}	
			
		}
		
		return processor.process(template);
		
	}
	
	
	

}
