package controllers.printservice;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.util.ByteString;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.print.DocumentReference;
import nl.idgis.geoide.commons.domain.print.PrintEvent;
import nl.idgis.geoide.commons.domain.print.PrintRequest;
import nl.idgis.geoide.util.Promises;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.Logger;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.RawBuffer;
import play.mvc.Http.RequestBody;
import play.mvc.Result;

public class Print extends Controller {

	private final PrintService printService;
	private final DocumentCache documentCache;
	private final StreamProcessor streamProcessor;
	
	@Inject
	public Print (final PrintService printService, final DocumentCache documentCache, final StreamProcessor streamProcessor) {
		if (printService == null) {
			throw new NullPointerException ("printService cannot be null");
		}
		if (documentCache == null) {
			throw new NullPointerException ("documentCache cannot be null");
		}
		if (streamProcessor == null) {
			throw new NullPointerException ("streamProcessor cannot be null");
		}
		
		this.printService = printService;
		this.documentCache = documentCache;
		this.streamProcessor = streamProcessor;
	}
	
	public Promise<Result> printGet (final String u) throws Throwable {
		Logger.debug ("Printing URL: " + u);
		
		final URI uri = new URI (u);
		
		if (!"http".equals (uri.getScheme ()) && !"https".equals (uri.getScheme ())) {
			return Promise.pure ((Result) badRequest ("Bad protocol: " + uri.getScheme ()));
		}
		
		return doPrint (uri, uri);
	}
	
	@BodyParser.Of (value = BodyParser.Raw.class, maxLength = 10 * 1024 * 1024)
	public Promise<Result> printPost (final String u) throws Throwable {
		Logger.debug ("Printing from POST: " + u);
		
		final URI uri = new URI (u);
		
		if (!"http".equals (uri.getScheme ()) && !"https".equals (uri.getScheme ())) {
			return Promise.pure ((Result) badRequest ("Bad protocol: " + uri.getScheme ()));
		}

		final URI documentUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		
		final RequestBody body = request ().body ();
		final RawBuffer buffer = body.asRaw ();
		final File file = buffer.asFile ();
		
		return Promises.asPromise (documentCache
			.store (documentUri, new MimeContentType ("text/html"), new FileInputStream (file)))
			.flatMap (new Function<Document, Promise<Result>> () {
				@Override
				public Promise<Result> apply (final Document a) throws Throwable {
					return doPrint (documentUri, uri);
				}
			});
	}
	
	private Promise<Result> doPrint (final URI documentUri, final URI sourceUri) throws Throwable {
		final Promise<Publisher<PrintEvent>> eventStreamPromise = Promises.asPromise (printService.print (new PrintRequest (
				new DocumentReference (new MimeContentType ("text/html"), documentUri), 
				new MimeContentType ("application/pdf"), 
				getBaseUri (sourceUri)
			)));
		
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
					if (event.getException ().isPresent ()) {
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
				final Publisher<ByteString> body = streamProcessor.resolvePublisherReference (document.getBody (), 5000);
				return ok (streamProcessor.asInputStream (body, 1024)).as (document.getContentType ().original ());
			});
		});
	}
	
	private static URI getBaseUri (final URI uri) throws URISyntaxException {
		final String path = uri.getPath ();
		final String newPath;
		
		if (path == null) {
			newPath = "/";
		} else if (path.endsWith ("/")) {
			newPath = path;
		} else {
			final int offset = path.lastIndexOf ('/');
			if (offset >= 0) {
				newPath = path.substring (0, offset + 1);
			} else {
				newPath = "/";
			}
		}
		
		return new URI (uri.getScheme (), uri.getUserInfo (), uri.getHost (), uri.getPort (), newPath, null, null);
	}
}
