package controllers.printservice;


import nl.idgis.geoide.commons.report.ReportComposer;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;



public class Report extends Controller {
	private final ReportComposer composer;
	private final StreamProcessor streamProcessor;


	public Report(ReportComposer reportComposer, StreamProcessor streamProcessor) {
		if (reportComposer == null) {
			throw new NullPointerException ("reportComposer cannot be null");
		}
		if (streamProcessor == null) {
			throw new NullPointerException ("streamProcessor cannot be null");
		}
		this.composer = reportComposer;	
		this.streamProcessor = streamProcessor;
	}




	public Promise<Result> report () throws Throwable {
		return doCompose(request ().body ().asJson ());		
	}
	
	private Promise<Result> doCompose (final JsonNode reportJson) throws Throwable {
		
		final Promise<Document> documentPromise = composer.compose(reportJson);
		
		return documentPromise.map (new Function<Document, Result> () {
			@Override
			public Result apply (final Document document) throws Throwable {
				return ok (streamProcessor.asInputStream (document.getBody (), 1024)).as (document.getContentType ().original ());
			}
		});
	}
	

	

}
