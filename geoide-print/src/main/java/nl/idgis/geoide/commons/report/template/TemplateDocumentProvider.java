package nl.idgis.geoide.commons.report.template;

import java.io.IOException;

import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;


public interface TemplateDocumentProvider {

	Promise<TemplateDocument> getTemplateDocument(String templateUrl) throws IOException;
	
	Promise<JsonNode> getTemplates();
	

	Promise<JsonNode> getTemplateProperties(String templateName,
			Promise<TemplateDocument> html);
	
}
