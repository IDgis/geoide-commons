package controllers.printservice;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import nl.idgis.geoide.commons.print.common.DocumentReference;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.commons.print.service.PrintService;
import nl.idgis.geoide.documentcache.CachedDocument;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.ogc.util.MimeContentType;
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
	
	public Print (final PrintService printService, final DocumentCache documentCache) {
		if (printService == null) {
			throw new NullPointerException ("printService cannot be null");
		}
		if (documentCache == null) {
			throw new NullPointerException ("documentCache cannot be null");
		}
		
		this.printService = printService;
		this.documentCache = documentCache;
	}
	
	public Promise<Result> printGet (final String u) throws Throwable {
		Logger.debug ("Printing URL: " + u);
		
		final URI uri = new URI (u);
		
		if (!"http".equals (uri.getScheme ()) && !"https".equals (uri.getScheme ())) {
			return Promise.pure ((Result) badRequest ("Bad protocol: " + uri.getScheme ()));
		}
		
		return doPrint (uri, uri);
	}
	
	@BodyParser.Of (BodyParser.Raw.class)
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
		
		return documentCache
			.store (documentUri, new MimeContentType ("text/html"), new FileInputStream (file))
			.flatMap (new Function<CachedDocument, Promise<Result>> () {
				@Override
				public Promise<Result> apply (final CachedDocument a) throws Throwable {
					return doPrint (documentUri, uri);
				}
			});
	}
	
	private Promise<Result> doPrint (final URI documentUri, final URI sourceUri) throws Throwable {
		
		final Promise<CachedDocument> documentPromise = printService.print (new PrintRequest (
				new DocumentReference (new MimeContentType ("text/html"), documentUri), 
				new MimeContentType ("application/pdf"), 
				getBaseUri (sourceUri)
			));

		return documentPromise.map (new Function<CachedDocument, Result> () {
			@Override
			public Result apply (final CachedDocument document) throws Throwable {
				return ok (document.asInputStream ()).as (document.getContentType ().original ());
			}
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
