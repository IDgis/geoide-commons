package controllers.printservice;


import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.print.PrintException;
import nl.idgis.geoide.commons.domain.report.LessCompilationException;
import nl.idgis.geoide.util.Promises;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.Application;
import play.Logger;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;



public class Report extends Controller {
	private final Logger.ALogger log = Logger.of (Report.class);
	
	private final ReportComposer composer;
	private final StreamProcessor streamProcessor;
	private final DocumentCache documentCache;
	private final Application application;
	private final MessagesApi messages;

	@Inject
	public Report (
			ReportComposer reportComposer, 
			StreamProcessor streamProcessor, 
			DocumentCache documentCache, 
			final Application application,
			final MessagesApi messages) {
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
		this.application = Objects.requireNonNull (application, "application cannot be null");
		this.messages = Objects.requireNonNull (messages, "messages cannot be null");
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
		
		return documentPromise.map (document -> {
			Logger.debug ("Received document: " + (document == null ? "null" : document.getClass ().getCanonicalName ()));
			// Build response:
			final ObjectNode result = Json.newObject ();
			result.put ("result", "ok");
			result.put("reportUrl", document.getUri().toString());
			return (Result) ok (result);
		}).recover (throwable -> {
			log.error ("Report request returned error", throwable);
			
			for (final URL url: Collections.list (application.classloader ().getResources ("messages"))) {
				log.error ("Messages: " + url);
			}

			log.error ("A: " + messages.get (Lang.defaultLang (), "a"));
			
			return internalServerError (reportError (throwable, messages.preferred (request ())));
		});
	}
	
	/**
	 * Transforms an error received during report generation into a JSON object containing
	 * a human readable message and technical details.
	 * 
	 * @param t		The exception to translate.
	 * @return		A JSON object containing the details that need to be reported to the client.
	 */
	private JsonNode reportError (final Throwable t, final Messages messages) {
		final ObjectNode result = Json.newObject ();
		
		result.put ("result", "error");
		result.put ("message", t.getMessage ());
		
		if (t instanceof PrintException.ResourceNotFound) {
			result.put ("type", "resourceNotFound");
			result.put ("resourceReference", ((PrintException.ResourceNotFound) t).getResourceReference ());
		} else if (t instanceof PrintException.UnsupportedFormat) {
			result.put ("type", "unsupportedFormat");
			result.put ("inputFormat", ((PrintException.UnsupportedFormat) t).getInputFormat().toString ());
			result.put ("outputFormat", ((PrintException.UnsupportedFormat) t).getOutputFormat ().toString ());
		} else if (t instanceof PrintException) {
			result.put ("type", "printError");
		} else if (t instanceof LessCompilationException) {
			result.put ("type", "lessCompilation");
			result.put ("filename", ((LessCompilationException) t).getFilename ());
			result.put ("line", ((LessCompilationException) t).getLine ());
			result.put ("column", ((LessCompilationException) t).getColumn ());
			final ArrayNode extract = result.putArray ("extract");
			for (final String s: ((LessCompilationException) t).getExtract ()) {
				extract.add (s);
			}
		} else {
			result.put ("type", "other");
		}
		
		return result;
	}
}
