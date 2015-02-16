package nl.idgis.geoide.commons.report.template;

import java.io.IOException;


public interface TemplateDocumentProvider {

	TemplateDocument getTemplateDocument(String templateUrl) throws IOException;
	
}
