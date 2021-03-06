package controllers.printservice;


import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.util.ByteString;
import i18n.printservice.ReportExceptions;
import models.core.GeoideMessages;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.document.DocumentCacheException;
import nl.idgis.geoide.commons.domain.print.PrintEvent;
import nl.idgis.geoide.commons.domain.print.PrintException;
import nl.idgis.geoide.commons.domain.report.LessCompilationException;
import nl.idgis.geoide.util.Promises;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.Logger;
import play.i18n.MessagesApi;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;



public class Report extends Controller {
	private final Logger.ALogger log = Logger.of (Report.class);
	
	private final ReportComposer composer;
	private final StreamProcessor streamProcessor;
	private final DocumentCache documentCache;
	private final MessagesApi messages;
	private final GeoideMessages geoideMessages;

	@Inject
	Report (
			ReportComposer reportComposer, 
			StreamProcessor streamProcessor, 
			DocumentCache documentCache, 
			final MessagesApi messages,
			final GeoideMessages geoideMessages) {
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
		this.messages = Objects.requireNonNull (messages, "messages cannot be null");
		this.geoideMessages = Objects.requireNonNull (geoideMessages, "geoideMessages cannot be null");
	}

	public Promise<Result> fetchReport (final String u) throws Throwable {
		
		URI uri = new URI(u);
		final Promise<Document> documentPromise = Promises.asPromise (documentCache.fetch(uri));

		
		
		return documentPromise.map (new Function<Document, Result> () {
			//@Override
			public Result apply (final Document document) throws Throwable {
				final Publisher<ByteString> body = streamProcessor.resolvePublisherReference (document.getBody (), 5000);
				return ok (streamProcessor.asInputStream (body, 1024)).as (document.getContentType ().original ());
			}
		});
	}


	public Promise<Result> report () throws Throwable {
		return  doCompose(request ().body ().asJson ());		
	}
	
	private Promise<Result> doCompose (final JsonNode reportJson) throws Throwable {
		String token = request().cookies().get("configToken").value();
		
		String refererUrl = request().getHeader("referer");
		
		final Promise<Publisher<PrintEvent>> eventStreamPromise = Promises.asPromise (
				composer.compose (
						JsonFactory.externalize (reportJson), token));

		return eventStreamPromise.flatMap (eventStream -> {
			final CompletableFuture<Document> documentFuture = new CompletableFuture<> ();
			
			eventStream.subscribe (new Subscriber<PrintEvent> () {
				@Override
				public void onComplete () {
				}

				@Override
				public void onError (final Throwable t) {
					documentFuture.completeExceptionally (t);
				}

				@Override
				public void onNext (final PrintEvent event) {
					if (event.getException().isPresent ()) {
						documentFuture.completeExceptionally (event.getException ().get ());
					} else if (event.getDocument ().isPresent ()) {
						documentFuture.complete (event.getDocument ().get ());
					}
				}

				@Override
				public void onSubscribe (final Subscription subscription) {
					subscription.request (Long.MAX_VALUE);
				}
			});
			
			return Promises.asPromise (documentFuture).map (document -> {
				Logger.debug ("Received document: " + (document == null ? "null" : document.getClass ().getCanonicalName ()));
				// Build response:
				final ObjectNode result = Json.newObject ();
				result.put ("result", "ok");
				result.put("reportUrl", document.getUri().toString());
				result.put("refererUrl", refererUrl);
				return (Result) ok (result);
				
			});
		}).recover ((Throwable throwable) -> {
			log.error ("Report request returned error", throwable);
			
			return internalServerError (reportError (
					throwable, 
					geoideMessages.of (ReportExceptions.class, messages.preferred (request ()))));
		});
	}
	
	/**
	 * Transforms an error received during report generation into a JSON object containing
	 * a human readable message and technical details.
	 * 
	 * @param t		The exception to translate.
	 * @return		A JSON object containing the details that need to be reported to the client.
	 */
	private JsonNode reportError (final Throwable t, final ReportExceptions messages) {
		final ObjectNode result = Json.newObject ();
		final Throwable cause;
		
		result.put ("result", "error");
		result.put ("message", t.getMessage ());
		
		if (t instanceof CompletionException) {
			cause = t.getCause () != null ? t.getCause () : t; 
		} else {
			cause = t;
		}
		
		if (cause instanceof PrintException.ResourceNotFound) {
			result.put ("type", "resourceNotFound");
			result.put ("resourceReference", ((PrintException.ResourceNotFound) cause).getResourceReference ());
			result.put ("localizedMessage", messages.resourceNotFound (((PrintException.ResourceNotFound) cause).getResourceReference ()));
		} else if (cause instanceof DocumentCacheException.DocumentNotFoundException) {
			result.put ("type", "resourceNotFound");
			result.put ("resourceReference", ((DocumentCacheException.DocumentNotFoundException) cause).getUri().toString ());
			result.put ("localizedMessage", messages.resourceNotFound (((DocumentCacheException.DocumentNotFoundException) cause).getUri ().toString ()));
		} else if (cause instanceof PrintException.UnsupportedFormat) {
			result.put ("type", "unsupportedFormat");
			result.put ("inputFormat", ((PrintException.UnsupportedFormat) cause).getInputFormat().toString ());
			result.put ("outputFormat", ((PrintException.UnsupportedFormat) cause).getOutputFormat ().toString ());
			result.put ("localizedMessage", messages.unsupportedFormat (((PrintException.UnsupportedFormat) cause).getInputFormat ().toString (), ((PrintException.UnsupportedFormat) cause).getOutputFormat ().toString ()));
		} else if (cause instanceof PrintException) {
			result.put ("type", "printError");
			result.put ("localizedMessage", messages.printError ());
		} else if (cause instanceof LessCompilationException) {
			result.put ("type", "lessCompilation");
			result.put ("filename", ((LessCompilationException) cause).getFilename ());
			result.put ("line", ((LessCompilationException) cause).getLine ());
			result.put ("column", ((LessCompilationException) cause).getColumn ());
			final ArrayNode extract = result.putArray ("extract");
			for (final String s: ((LessCompilationException) cause).getExtract ()) {
				extract.add (s);
			}
			result.put ("localizedMessage", messages.lessCompilation ());
		} else {
			result.put ("type", "other");
			result.put ("localizedMessage", messages.genericError ());
		}
		
		return result;
	}
}
