package controllers.printservice;

import nl.idgis.geoide.commons.report.template.HtmlTemplateDocumentProvider;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

public class Template extends Controller {
	private final HtmlTemplateDocumentProvider templateProvider;
	
	public Template(HtmlTemplateDocumentProvider templateProvider) {
		this.templateProvider = templateProvider;
		
		if (templateProvider  == null) {
			throw new NullPointerException ("templateProvider cannot be null");
		}
		
	}
		
	public	 Promise<Result> getTemplates () throws Throwable {

		final Promise<JsonNode> templatePromise = this.templateProvider.getTemplates();
		
		return templatePromise.map((templates) -> {
			return ok(templates);
		});
	}
	

}

