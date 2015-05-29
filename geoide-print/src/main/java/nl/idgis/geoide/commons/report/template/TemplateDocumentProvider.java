package nl.idgis.geoide.commons.report.template;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;


public interface TemplateDocumentProvider {

	CompletableFuture<TemplateDocument> getTemplateDocument(String templateUrl) throws IOException;
	
	CompletableFuture<JsonNode> getTemplates();
	

	CompletableFuture<JsonNode> getTemplateProperties(String templateName,
			CompletableFuture<TemplateDocument> html);
	
}
