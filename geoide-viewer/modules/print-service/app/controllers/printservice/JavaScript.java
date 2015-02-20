package controllers.printservice;

import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;

public class JavaScript extends Controller {

	public static Result javascriptRoutes () {
		response ().setContentType ("text/javascript");
		return ok (
			Routes.javascriptRouter ("geoideReportRoutes",
				controllers.printservice.routes.javascript.Report.report()
			)	
		);
	}
}
