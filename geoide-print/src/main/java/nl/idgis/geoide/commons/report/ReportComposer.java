package nl.idgis.geoide.commons.report;


import java.util.Iterator;
import java.util.ListIterator;

import nl.idgis.geoide.commons.report.blocks.TextBlockComposer;
import nl.idgis.geoide.commons.report.template.TemplateDocument;
import nl.idgis.geoide.commons.report.template.TemplateDocumentProvider;
import nl.idgis.geoide.documentcache.Document;

import org.jsoup.nodes.Element;

import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;

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
	
	
	/**
	 * Constructs a report composer with a series of specialized copomsers
	 * 
	 * @param postprocessor 	the postprocessor to send the report to, before printing
	 * @param templateprovider 	the template provider to retrieve the report template from. 
	 */
	
	public ReportComposer (ReportPostProcessor processor, TemplateDocumentProvider templateProvider) {
		this.processor =  processor;
		this.templateProvider = templateProvider;
		this.textBlockComposer = new TextBlockComposer();
	}
	
	/**
	 * Composes a report for printing and sends the composed report to the postprocessor.
	 * 
	 * @param reportInfo 	client information in the form of a Json Node. 
	 */
	public Promise<Document> compose (JsonNode reportInfo) throws Throwable {
		String templateUrl = "templates/template-test.html";
		
		TemplateDocument template = templateProvider.getTemplateDocument(templateUrl);
		System.out.println("template " + template.toString());
		
		Iterator<Element> blocks =  template.getBlocks().iterator();
		
		while (blocks.hasNext()){
			Element block = blocks.next();
			if(	block.hasClass("text")){
				textBlockComposer.compose(reportInfo, block);
			}
		}; 
		

		return processor.process(template);
		
	}
	

}
