package nl.idgis.geoide.commons.domain.api;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;


public interface TemplateDocumentProvider {

	CompletableFuture<TemplateDocument> getTemplateDocument(String templateUrl) throws IOException;
	
	CompletableFuture<ExternalizableJsonNode> getTemplates();
	
	CompletableFuture<ExternalizableJsonNode> getTemplateProperties(TemplateDocument template);
}
