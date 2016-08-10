package controllers.core;

import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;

public class JavaScript extends Controller {

	public static Result javascriptRoutes () {
		response ().setContentType ("text/javascript");
		return ok (
			Routes.javascriptRouter ("geoideCoreRoutes",
				controllers.core.routes.javascript.MapConfiguration.mapStructure (),
				controllers.core.routes.javascript.Image.getImage()
			)	
		);
	}
}
