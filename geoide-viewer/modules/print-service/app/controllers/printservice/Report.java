package controllers.printservice;


import play.mvc.Controller;
import play.mvc.Result;
import nl.idgis.geoide.commons.report.ReportComposer;



public class Report extends Controller {
	private final ReportComposer composer;


	public Report(ReportComposer reportComposer) {
		this.composer = reportComposer;	
	}




	public Result report () {
		composer.compose(request ().body ().asJson ());		
		return ok();
	
	}
	

	

}
