package controllers.printservice;

import nl.idgis.geoide.commons.report.template.TemplateDocumentProvider;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

public class Template extends Controller {
	private final TemplateDocumentProvider templateProvider;
	
	public Template(TemplateDocumentProvider templateProvider) {
		this.templateProvider = templateProvider;
		
		if (templateProvider  == null) {
			throw new NullPointerException ("templateProvider cannot be null");
		}
		
	}
		
	public	 Promise<Result> getTemplates () throws Throwable {

		final Promise<JsonNode> templatePromise = Promises.asPromise (this.templateProvider.getTemplates());
		
		return templatePromise.map((templates) -> {
			return ok(templates);
		});
	}
	

}

