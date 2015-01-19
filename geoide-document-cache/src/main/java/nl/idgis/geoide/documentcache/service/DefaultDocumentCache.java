package nl.idgis.geoide.documentcache.service;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.idgis.geoide.documentcache.ByteStringCachedDocument;
import nl.idgis.geoide.documentcache.CachedDocument;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.ogc.util.MimeContentType;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import play.libs.F.Callback;
import play.libs.F.Function;
import play.libs.F.Promise;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

public class DefaultDocumentCache implements DocumentCache, Closeable {

	private final long ttlInSeconds;
	private final DocumentStore readThroughStore;
	private final ActorRef cacheActor;
	private final ActorRefFactory actorRefFactory;

	private DefaultDocumentCache (final ActorRefFactory actorRefFactory, final String cacheName, final long ttlInSeconds, final File file, final Double maxSizeInGigabytes, final DocumentStore readThroughStore) {
		if (file == null && maxSizeInGigabytes == null) {
			throw new IllegalArgumentException ("Either file or maxSizeInGigabytes must be given");
		}
		
		this.ttlInSeconds = ttlInSeconds;
		this.readThroughStore = readThroughStore;
		this.actorRefFactory = actorRefFactory;
		cacheActor = actorRefFactory.actorOf (CacheActor.props (ttlInSeconds, file, maxSizeInGigabytes, readThroughStore), String.format ("geoide-cache-%s", cacheName));
	}
	
	public static DefaultDocumentCache createInMemoryCache (final ActorSystem actorSystem, final String cacheName, final long ttlSeconds, final double maxSizeInGigabytes, final DocumentStore readThroughStore) {
		return new DefaultDocumentCache (actorSystem, cacheName, ttlSeconds, null, maxSizeInGigabytes, readThroughStore);
	}
	
	public static DefaultDocumentCache createTempFileCache (final ActorSystem actorSystem, final String cacheName, final long ttlSeconds, final DocumentStore readThroughStore) throws IOException {
		final File file = File.createTempFile ("geoide-cache-", ".tmp.db");
		
		file.deleteOnExit ();
		
		return createFileCache (actorSystem, cacheName, ttlSeconds, file, readThroughStore);
	}
	
	public static DefaultDocumentCache createFileCache (final ActorSystem actorSystem, final String cacheName, final long ttlSeconds, final File file, final DocumentStore readThroughStore) {
		return new DefaultDocumentCache (actorSystem, cacheName, ttlSeconds, file, null, readThroughStore);
	}
	
	@Override
	public Promise<CachedDocument> store (final URI uri) {
		if (readThroughStore == null) {
			// Without a readThrough store, the document can never be fetched:
			return Promise.throwing (new DocumentCacheException.DocumentNotFoundException (uri));
		}
		
		// Force an update of the object in the cache:
		return fetch (uri, true);
	}

	private Promise<CachedDocument> store (final CachedDocument document) {
		return askCache (new StoreDocument (document)).map (new Function<Object, CachedDocument> () {
			@Override
			public CachedDocument apply (final Object message) throws Throwable {
				if (message instanceof DocumentStored) {
					return ((DocumentStored) message).getDocument ();
				} else if (message instanceof CacheMiss) {
					final Throwable cause = ((CacheMiss) message).getCause ();
					if (cause != null) {
						throw cause;
					} else {
						throw new DocumentCacheException ("Unknown error while storing: " + ((CacheMiss) message).getUri ().toString ());
					}
				} else {
					throw new IllegalArgumentException ("Unexpected response: " + message.getClass ().getCanonicalName ());
				}
			}
		});
	}
	
	@Override
	public Promise<CachedDocument> store (final URI uri, final MimeContentType contentType, final byte[] data) {
		final CachedDocument document = new ByteStringCachedDocument (
				uri, 
				contentType, 
				ByteStrings.fromArray (data)
			);

		return store (document);
	}

	@Override
	public Promise<CachedDocument> store (final URI uri, final MimeContentType contentType, final InputStream inputStream) {
		final CachedDocument document;
		
		try {
			ByteString byteString = ByteStrings.empty ();
			
			byte[] data = new byte[4096];
			int nRead;
			
			while ((nRead = inputStream.read (data, 0, data.length)) != -1) {
				byteString = byteString.concat (ByteStrings.fromArray (data, 0, nRead));
			}
			
			inputStream.close ();
			
			document = new ByteStringCachedDocument (
				uri,
				contentType,
				byteString
			);
		} catch (IOException e) {
			return Promise.throwing (new DocumentCacheException.IOError (e));
		}
		
		return store (document);
	}

	@Override
	public Promise<CachedDocument> fetch (final URI uri) {
		return fetch (uri, false);
	}
	
	private Promise<CachedDocument> fetch (final URI uri, final boolean forceUpdate) {
		return askCache (new FetchDocument (uri, forceUpdate)).map (new Function<Object, CachedDocument> () {
			@Override
			public CachedDocument apply (final Object message) throws Throwable {
				if (message instanceof CachedDocument) {
					return (CachedDocument) message;
				} else if (message instanceof DocumentStored) {
					return ((DocumentStored) message).getDocument ();
				} else if (message instanceof CacheMiss) {
					throw new DocumentCacheException.DocumentNotFoundException (uri, ((CacheMiss) message).getCause ());
				} else {
					throw new IllegalArgumentException ("Unknown message type: " + message.getClass ().getCanonicalName ());
				}
			}
		});
	}

	@Override
	public Promise<Long> getTtl () {
		return Promise.pure (ttlInSeconds);
	}
	
	private Promise<Object> askCache (final Object message) {
		return Promise.wrap (Patterns.ask (cacheActor, message, 15000));
	}
	
	@Override
	public void close() throws IOException {
		actorRefFactory.stop (cacheActor);
	}
	
	public static class CacheActor extends UntypedActor {
		
		private final DocumentStore readThroughStore;
		private final long ttlInSeconds;
		private final File file;
		private final Double maxSizeInGigabytes;
		private final Map<URI, List<ActorRef>> waitLists = new HashMap<> ();
		
		private DB db;
		private HTreeMap<URI, CachedDocument> cache;
		
		public CacheActor (final long ttlInSeconds, final File file, final Double maxSizeInGigabytes, final DocumentStore readThroughStore) {
			if (file == null && maxSizeInGigabytes == null) {
				throw new IllegalArgumentException ("Either file or maxSizeInGigabytes must be given");
			}
			
			this.ttlInSeconds = ttlInSeconds;
			this.file = file;
			this.maxSizeInGigabytes = maxSizeInGigabytes;
			this.readThroughStore = readThroughStore;
		}
		
		public static Props props (final long ttl, final File file, final Double maxSizeInGigabytes, final DocumentStore readThroughStore) { 
			return Props.create (CacheActor.class, ttl, file, maxSizeInGigabytes, readThroughStore);
		}
		
		@Override
		public void preStart () throws Exception {
			// Create database:
			if (file != null) {
				// Create a file backed database:
				db = DBMaker
					.newFileDB (file)
					.make ();
			} else {
				// Create a memory backed database (off-heap):
				db = DBMaker
					.newMemoryDirectDB ()
					.sizeLimit (maxSizeInGigabytes)
					.make ();
			}

			// Create cache map:
			cache = db
				.createHashMap ("cache")
				.expireAfterWrite (ttlInSeconds, TimeUnit.SECONDS)
				.expireAfterAccess (ttlInSeconds, TimeUnit.SECONDS)
				.<URI, CachedDocument>make ();

		}
		
		@Override
		public void postStop () throws Exception {
			db.close ();
		}
		
		@Override
		public void onReceive (final Object message) throws Exception {
			if (message instanceof FetchDocument) {
				final URI uri = ((FetchDocument) message).getUri ();
				
				// Return from the cache:
				if (!((FetchDocument) message).isForceUpdate () && cache.containsKey (uri)) {
					sender ().tell (cache.get (uri), self ());
					return;
				}
				
				// See if the document is already being fetched from the readthrough store:
				if (waitLists.containsKey (uri)) {
					// Add the sender to the wait list and return:
					waitLists.get (uri).add (sender ());
					return;
				}
				
				// Send a cache miss if the document doesn't exist and can't be fetched:
				if (readThroughStore == null) {
					sender ().tell (new CacheMiss (uri), self ());
					return;
				}
					
				// Request the document from the read through store:
				waitLists.put (uri, new ArrayList<ActorRef> ());
				waitLists.get (uri).add (sender ());
			
				fetchRemote (uri);
			} else if (message instanceof CacheMiss) {
				// A read from the readthrough store failed:
				final URI uri = ((CacheMiss) message).getUri ();
				
				// Notify all waiters of the failure:
				notifyWaiters (uri, message, self ());
			} else if (message instanceof StoreDocument) {
				final CachedDocument document = ((StoreDocument) message).getDocument ();

				// Store the document in the cache:
				Object response = new DocumentStored (document);
				try {
					cache.put (document.getUri (), document);
				} catch (Throwable e) {
					response = new CacheMiss (document.getUri (), e);
				}
				
				// Notify all waiters that the document has arrived in the cache:
				notifyWaiters (document.getUri (), response, self ());
				
				// Signal success:
				sender ().tell (response, self ());
			} else {
				unhandled (message);
			}
		}
		
		private void notifyWaiters (final URI uri, final Object message, final ActorRef sender) {
			final List<ActorRef> waiters = waitLists.get (uri);
			if (waiters == null) {
				return;
			}
			
			for (final ActorRef waiter: waiters) {
				waiter.tell (message, sender);
			}
			
			waitLists.remove (uri);
		}
		
		private void fetchRemote (final URI uri) {
			final Promise<CachedDocument> promise = readThroughStore.fetch (uri);
			final ActorRef self = self ();
			
			promise.onFailure (new Callback<Throwable> () {
				@Override
				public void invoke (final Throwable cause) throws Throwable {
					self.tell (new CacheMiss (uri, cause), self);
				}
			});
			
			promise.onRedeem (new Callback<CachedDocument> () {
				@Override
				public void invoke (final CachedDocument document) throws Throwable {
					self.tell (new StoreDocument (document), self);
				}
			});
		}
	}
	
	public final static class SerializableContentType implements Serializable {
		private static final long serialVersionUID = -3936226269463354913L;
		
		private final String contentType;
		
		public SerializableContentType (final String contentType) {
			this.contentType = contentType;
		}
		
		public String getContentType () {
			return contentType;
		}
	}

	public final static class FetchDocument {
		private final URI uri;
		private final boolean forceUpdate;
		
		public FetchDocument (final URI uri, final boolean forceUpdate) {
			this.uri = uri;
			this.forceUpdate = forceUpdate;
		}
		
		public URI getUri () {
			return uri;
		}
		
		public boolean isForceUpdate () {
			return forceUpdate;
		}
	}
	
	public final static class StoreDocument {
		private final CachedDocument document;
		
		public StoreDocument (final CachedDocument document) {
			this.document = document;
		}
		
		public CachedDocument getDocument () {
			return document;
		}
	}
	
	public final static class CacheMiss {
		private final URI uri;
		private final Throwable cause;
		
		public CacheMiss (final URI uri) {
			this (uri, null);
		}
		
		public CacheMiss (final URI uri, final Throwable cause) {
			this.uri = uri;
			this.cause = cause;
		}
		
		public URI getUri () {
			return uri;
		}
		
		public boolean hasCause () {
			return cause != null;
		}
		
		public Throwable getCause () {
			return cause;
		}
	}
	
	public final static class DocumentStored {
		private final CachedDocument document;
		
		public DocumentStored (final CachedDocument document) {
			this.document = document;
		}
		
		public CachedDocument getDocument () {
			return document;
		}
	}
}
