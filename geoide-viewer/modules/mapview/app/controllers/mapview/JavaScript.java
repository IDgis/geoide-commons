package controllers.mapview;

import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;

public class JavaScript extends Controller {

	public static Result javascriptRoutes () {
		response ().setContentType ("text/javascript");
		return ok (
			Routes.javascriptRouter ("geoideViewerRoutes",
				controllers.mapview.routes.javascript.View.buildView (),
				controllers.mapview.routes.javascript.Services.serviceRequest (),
				controllers.mapview.routes.javascript.Services.serviceRequestWithLayer (),
				controllers.mapview.routes.javascript.Query.query ()
			)	
		);
	}
}
