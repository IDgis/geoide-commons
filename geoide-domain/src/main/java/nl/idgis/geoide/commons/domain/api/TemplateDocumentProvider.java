package nl.idgis.geoide.commons.domain.api;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.report.TemplateDocument;

import com.fasterxml.jackson.databind.JsonNode;


public interface TemplateDocumentProvider {

	CompletableFuture<TemplateDocument> getTemplateDocument(String templateUrl) throws IOException;
	
	CompletableFuture<JsonNode> getTemplates();
	
	CompletableFuture<JsonNode> getTemplateProperties(TemplateDocument template);
}
