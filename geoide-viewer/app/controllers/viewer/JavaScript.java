package controllers.viewer;

import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;

public class JavaScript extends Controller {

	public static Result javascriptRoutes () {
		response ().setContentType ("text/javascript");
		return ok (
			Routes.javascriptRouter ("planoviewViewerRoutes",
				controllers.viewer.routes.javascript.MapConfiguration.mapStructure (),
				controllers.viewer.routes.javascript.View.buildView (),
				controllers.viewer.routes.javascript.Services.serviceRequest (),
				controllers.viewer.routes.javascript.Services.serviceRequestWithLayer (),
				controllers.viewer.routes.javascript.Query.query ()
			)	
		);
	}
}
