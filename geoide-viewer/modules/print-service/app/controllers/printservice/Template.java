package controllers.printservice;

import javax.inject.Inject;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.api.TemplateDocumentProvider;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

public class Template extends Controller {
	private final TemplateDocumentProvider templateProvider;
	
	@Inject
	public Template(TemplateDocumentProvider templateProvider) {
		this.templateProvider = templateProvider;
		
		if (templateProvider  == null) {
			throw new NullPointerException ("templateProvider cannot be null");
		}
		
	}
		
	public	 Promise<Result> getTemplates () throws Throwable {

		final Promise<ExternalizableJsonNode> templatePromise = Promises.asPromise (this.templateProvider.getTemplates());
		
		return templatePromise.map((templates) -> {
			return ok(templates.getJsonNode ());
		});
	}
	

}

