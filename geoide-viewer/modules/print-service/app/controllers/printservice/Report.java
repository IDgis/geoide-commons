package controllers.printservice;


import java.net.URI;

import javax.inject.Inject;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.util.Promises;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.Logger;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;



public class Report extends Controller {
	private final ReportComposer composer;
	private final StreamProcessor streamProcessor;
	private final DocumentCache documentCache;

	@Inject
	public Report(ReportComposer reportComposer, StreamProcessor streamProcessor, DocumentCache documentCache) {
		if (reportComposer == null) {
			throw new NullPointerException ("reportComposer cannot be null");
		}
		if (streamProcessor == null) {
			throw new NullPointerException ("streamProcessor cannot be null");
		}
		if (documentCache == null) {
			throw new NullPointerException ("documentCache cannot be null");
		}
		this.composer = reportComposer;	
		this.streamProcessor = streamProcessor;
		this.documentCache = documentCache;
	}

	public Promise<Result> fetchReport (final String u) throws Throwable {
		
		URI uri = new URI(u);
		final Promise<Document> documentPromise = Promises.asPromise (documentCache.fetch(uri));
		
		return documentPromise.map (new Function<Document, Result> () {
			//@Override
			public Result apply (final Document document) throws Throwable {
				return ok (streamProcessor.asInputStream (document.getBody (), 1024)).as (document.getContentType ().original ());
			}
		});
	}


	public Promise<Result> report () throws Throwable {
		return  doCompose(request ().body ().asJson ());		
	}
	
	private Promise<Result> doCompose (final JsonNode reportJson) throws Throwable {

		final Promise<Document> documentPromise = Promises.asPromise (composer.compose(JsonFactory.externalize (reportJson)));
		
		return documentPromise.map (new Function<Document, Result> () {
			//@Override
			public Result apply (final Document document) throws Throwable {
				Logger.debug ("Received document: " + (document == null ? "null" : document.getClass ().getCanonicalName ()));
				// Build response:
				final ObjectNode result = Json.newObject ();
				result.put ("result", "ok");
				result.put("reportUrl", document.getUri().toString());
				return ok (result);
				//return ok (streamProcessor.asInputStream (document.getBody (), 1024)).as (document.getContentType ().original ());
			}
		});
	}
	

	

}
