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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.DocumentStore;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.util.Futures;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.reactivestreams.Publisher;

import play.Logger;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;
import akka.util.CompactByteString;

/**
 * {@link DefaultDocumentCache} is an implementation of DocumentCache that temporarily stores documents in
 * a store provided by MapDB and using an Akka actor system for scheduling.
 */
public class DefaultDocumentCache implements DocumentCache, Closeable {

	private final long ttlInSeconds;
	private final DocumentStore readThroughStore;
	private final ActorRef cacheActor;
	private final ActorRefFactory actorRefFactory;
	private final StreamProcessor streamProcessor;

	private DefaultDocumentCache (final ActorRefFactory actorRefFactory, final StreamProcessor streamProcessor, final String cacheName, final long ttlInSeconds, final File file, final Double maxSizeInGigabytes, final DocumentStore readThroughStore, final int streamBlockSize) {
		if (file == null && maxSizeInGigabytes == null) {
			throw new IllegalArgumentException ("Either file or maxSizeInGigabytes must be given");
		}
		if (streamProcessor == null) {
			throw new NullPointerException ("streamProcessor cannot be null");
		}
		if (streamBlockSize <= 0) {
			throw new IllegalArgumentException ("streamBlockSize should be > 0");
		}
		
		this.ttlInSeconds = ttlInSeconds;
		this.readThroughStore = readThroughStore;
		this.actorRefFactory = actorRefFactory;
		cacheActor = actorRefFactory.actorOf (CacheActor.props (ttlInSeconds, file, maxSizeInGigabytes, readThroughStore, streamProcessor, streamBlockSize), String.format ("geoide-cache-%s", cacheName));
		this.streamProcessor = streamProcessor;
	}

	/**
	 * Creates an in-memory cache with an optional read-through store.
	 * 
	 * @param actorRefFactory		The factory used to create actors for scheduling. 
	 * @param streamProcessor		The stream processor used to process streams that are consumed and produced by this cache.
	 * @param cacheName				The name of the cache. The cache name is reflected in the names of the scheduling actors and the name of the MapDB store. 
	 * @param ttlSeconds			The number of seconds documents should (at least) be retained in the cache.
	 * @param maxSizeInGigabytes	The maximum size in gigabytes of the in-memory cache. When this size is exceeded, storing new documents will fail with an exception.
	 * @param readThroughStore		An optional read-through store that is accessed when fetching documents from the cache that have no entry.
	 * @param streamBlockSize		Maximum size of blocks when streaming cached documents. 
	 * @return						The created DefaultDocumentCache instance.
	 */
	public static DefaultDocumentCache createInMemoryCache (final ActorRefFactory actorRefFactory, final StreamProcessor streamProcessor, final String cacheName, final long ttlSeconds, final double maxSizeInGigabytes, final DocumentStore readThroughStore, final int streamBlockSize) {
		return new DefaultDocumentCache (actorRefFactory, streamProcessor, cacheName, ttlSeconds, null, maxSizeInGigabytes, readThroughStore, streamBlockSize);
	}
	
	/**
	 * Creates a cache of arbitrary size in a temporary file. The location of the temporary file is platform dependent.
	 *  
	 * @param actorRefFactory		The factory used to create actors for scheduling. 
	 * @param streamProcessor		The stream processor used to process streams that are consumed and produced by this cache.
	 * @param cacheName				The name of the cache. The cache name is reflected in the names of the scheduling actors and the name of the MapDB store. 
	 * @param ttlSeconds			The number of seconds documents should (at least) be retained in the cache.
	 * @param readThroughStore		An optional read-through store that is accessed when fetching documents from the cache that have no entry. 
	 * @param streamBlockSize		Maximum size of blocks when streaming cached documents. 
	 * @return						The created DefaultDocumentCache instance.
	 * @throws IOException			Thrown when the temporary file could not be created.
	 */
	public static DefaultDocumentCache createTempFileCache (final ActorRefFactory actorRefFactory, final StreamProcessor streamProcessor, final String cacheName, final long ttlSeconds, final DocumentStore readThroughStore, final int streamBlockSize) throws IOException {
		final File file = File.createTempFile ("geoide-cache-", ".tmp.db");
		
		file.deleteOnExit ();
		
		return createFileCache (actorRefFactory, streamProcessor, cacheName, ttlSeconds, file, readThroughStore, streamBlockSize);
	}
	
	/**
	 * 
	 * @param actorRefFactory		The factory used to create actors for scheduling. 
	 * @param streamProcessor		The stream processor used to process streams that are consumed and produced by this cache.
	 * @param cacheName				The name of the cache. The cache name is reflected in the names of the scheduling actors and the name of the MapDB store. 
	 * @param ttlSeconds			The number of seconds documents should (at least) be retained in the cache.
	 * @param file					The file to create a database in.
	 * @param readThroughStore		An optional read-through store that is accessed when fetching documents from the cache that have no entry. 
	 * @param streamBlockSize		Maximum size of blocks when streaming cached documents. 
	 * @return						The created DefaultDocumentCache instance.
	 */
	public static DefaultDocumentCache createFileCache (final ActorRefFactory actorRefFactory, final StreamProcessor streamProcessor, final String cacheName, final long ttlSeconds, final File file, final DocumentStore readThroughStore, final int streamBlockSize) {
		return new DefaultDocumentCache (actorRefFactory, streamProcessor, cacheName, ttlSeconds, file, null, readThroughStore, streamBlockSize);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Document> store (final URI uri) {
		if (readThroughStore == null) {
			// Without a readThrough store, the document can never be fetched:
			return Futures.throwing (new DocumentCacheException.DocumentNotFoundException (uri));
		}
		
		// Force an update of the object in the cache:
		return fetch (uri, true);
	}

	private CompletableFuture<Document> store (final Document document) {
		return askCache (new StoreDocument (document)).thenApply ((message) -> {
			if (message instanceof DocumentStored) {
				return ((DocumentStored) message).getDocument ();
			} else if (message instanceof CacheMiss) {
				final Throwable cause = ((CacheMiss) message).getCause ();
				if (cause != null) {
					throw new RuntimeException (cause);
				} else {
					throw new DocumentCacheException ("Unknown error while storing: " + ((CacheMiss) message).getUri ().toString ());
				}
			} else {
				throw new IllegalArgumentException ("Unexpected response: " + message.getClass ().getCanonicalName ());
			}
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Document> store (final URI uri, final MimeContentType contentType, final Publisher<ByteString> body) {
		if (uri == null) {
			throw new NullPointerException ("uri cannot be null");
		}
		if (contentType == null) {
			throw new NullPointerException ("contentType cannot be null");
		}
		if (body == null) {
			throw new NullPointerException ("body cannot be null");
		}
		
		return store (new Document () {
			@Override
			public URI getUri () {
				return uri;
			}
			
			@Override
			public MimeContentType getContentType () {
				return contentType;
			}
			
			@Override
			public Publisher<ByteString> getBody () {
				return body;
			}
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Document> store (final URI uri, final MimeContentType contentType, final byte[] data) {
		return store (uri, contentType, streamProcessor.<ByteString>publishSinglevalue (ByteStrings.fromArray (data)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Document> store (final URI uri, final MimeContentType contentType, final InputStream inputStream) {
		return store (uri, contentType, streamProcessor.publishInputStream (inputStream, 1024, 30000));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Document> fetch (final URI uri) {
		return fetch (uri, false);
	}
	
	private CompletableFuture<Document> fetch (final URI uri, final boolean forceUpdate) {
		return askCache (new FetchDocument (uri, forceUpdate)).thenApply ((message) -> {
			if (message instanceof Document) {
				return (Document) message;
			} else if (message instanceof DocumentStored) {
				return ((DocumentStored) message).getDocument ();
			} else if (message instanceof CacheMiss) {
				throw new DocumentCacheException.DocumentNotFoundException (uri, ((CacheMiss) message).getCause ());
			} else {
				throw new IllegalArgumentException ("Unknown message type: " + message.getClass ().getCanonicalName ());
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Long> getTtl () {
		return CompletableFuture.completedFuture (ttlInSeconds);
	}
	
	private CompletableFuture<Object> askCache (final Object message) {
		final Future<Object> scalaFuture = Patterns.ask (cacheActor, message, 15000);
		final CompletableFuture<Object> future = new CompletableFuture<> ();
		
		scalaFuture.onComplete (new OnComplete<Object> () {

			@Override
			public void onComplete (final Throwable t, final Object message) throws Throwable {
				if (t != null) {
					future.completeExceptionally (t);
				} else {
					future.complete (message);
				}
			}
			
		}, actorRefFactory.dispatcher ());
		
		return future;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		actorRefFactory.stop (cacheActor);
	}
	
	/**
	 * Akka actor to manage the cache. 
	 */
	public static class CacheActor extends UntypedActor {
		
		private final DocumentStore readThroughStore;
		private final StreamProcessor streamProcessor;
		private final long ttlInSeconds;
		private final File file;
		private final Double maxSizeInGigabytes;
		private final Map<URI, List<ActorRef>> waitLists = new HashMap<> ();
		private final int streamBlockSize;
		
		private DB db;
		private HTreeMap<URI, ByteStringCachedDocument> cache;
		
		public CacheActor (final long ttlInSeconds, final File file, final Double maxSizeInGigabytes, final DocumentStore readThroughStore, final StreamProcessor streamProcessor, final int streamBlockSize) {
			if (file == null && maxSizeInGigabytes == null) {
				throw new IllegalArgumentException ("Either file or maxSizeInGigabytes must be given");
			}
			if (streamBlockSize <= 0) {
				throw new IllegalArgumentException ("streamBlockSize must be > 0");
			}
			
			this.ttlInSeconds = ttlInSeconds;
			this.file = file;
			this.maxSizeInGigabytes = maxSizeInGigabytes;
			this.readThroughStore = readThroughStore;
			this.streamProcessor = streamProcessor;
			this.streamBlockSize = streamBlockSize;
		}
		
		public static Props props (final long ttl, final File file, final Double maxSizeInGigabytes, final DocumentStore readThroughStore, final StreamProcessor streamProcessor, final int streamBlockSize) { 
			return Props.create (CacheActor.class, ttl, file, maxSizeInGigabytes, readThroughStore, streamProcessor, streamBlockSize);
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
				.<URI, ByteStringCachedDocument>make ();

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
					Logger.debug ("Returning cached copy of: " + uri + " (" + cache.get (uri).getContentType () + ")");
					sender ().tell (createDocument (cache.get (uri)), self ());
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
					Logger.debug ("No readthrough store and no entry in cache for: " + uri.toString ());
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
				final Document document = ((StoreDocument) message).getDocument ();

				// Store the document in the cache:
				storeDocument (document);
			} else if (message instanceof StoreAndNotify) {
				final StoreAndNotify san = (StoreAndNotify) message;
				final Object response;
				final URI uri;
				
				if (san.getDocument () != null) {
					cache.put (san.getDocument ().getUri (), san.getDocument ());
					response = new DocumentStored (createDocument (san.getDocument ()));
					uri = san.getDocument().getUri ();
				} else {
					response = san.getMessage ();
					uri = san.getUri ();
				}
				
				// Notify all waiters that the document has arrived in the cache:
				notifyWaiters (uri, response, self ());
				
				// Signal the original sender:
				san.getSender ().tell (response, self ());
			} else {
				unhandled (message);
			}
		}
		
		private Document createDocument (final ByteStringCachedDocument cachedDocument) {
			return new Document () {
				@Override
				public URI getUri () {
					return cachedDocument.getUri ();
				}
				
				@Override
				public MimeContentType getContentType () {
					return new MimeContentType (cachedDocument.getContentType ());
				}
				
				@Override
				public Publisher<ByteString> getBody () {
					return streamProcessor.publishByteString (cachedDocument.getBody (), streamBlockSize);
				}
			};
		}
		
		private void storeDocument (final Document document) {
			
			final ActorRef self = self ();
			final ActorRef sender = sender ();
			
			// Reduce the document to a single value in memory:
			final CompletableFuture<ByteString> promise = streamProcessor.reduce (document.getBody (), ByteStrings.empty (), new BiFunction<ByteString, ByteString, ByteString> () {
				@Override
				public ByteString apply (final ByteString a, final ByteString b) {
					return a.concat (b);
				}
			});

			promise.handle ((body, throwable) -> {
				if (throwable == null) {
					try {
						self.tell (new StoreAndNotify (
								sender,
								null,
								new ByteStringCachedDocument (document.getUri (), document.getContentType ().original (), body.compact ()),
								document.getUri ()
							), self);
					} catch (Exception e) {
						throw new RuntimeException (e);
					}
				} else {
					try {
						self.tell (new StoreAndNotify (sender, new CacheMiss (document.getUri (), throwable), null, document.getUri ()), self);
					} catch (Exception e) {
						throw new RuntimeException (e);
					}
				}
				
				return null;
			});
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
			final CompletableFuture<Document> future = readThroughStore.fetch (uri);
			final ActorRef self = self ();
			
			future.handle ((document, throwable) -> {
				if (throwable != null) {
					Logger.debug ("Failed to fetch " + uri + " from readthrough store");
					self.tell (new CacheMiss (uri, throwable), self);
				} else {
					self.tell (new StoreDocument (document), self);
				}
				
				return null;
			});
		}
	}
	
	/**
	 * Serializable variant of the MimeContentType class, which by itself is not
	 * serializable. Stores the content type as a string.
	 */
	public final static class SerializableContentType implements Serializable {
		private static final long serialVersionUID = -3936226269463354913L;
		
		private final String contentType;

		/**
		 * Constructs a new ContentType.
		 * 
		 * @param contentType	The content type as a string value.
		 */
		public SerializableContentType (final String contentType) {
			this.contentType = contentType;
		}
		
		/**
		 * Returns the content type as a string value.
		 * 
		 * @return The content type.
		 */
		public String getContentType () {
			return contentType;
		}
	}

	public final static class StoreAndNotify {
		private final ActorRef sender;
		private final Object message;
		private final ByteStringCachedDocument document;
		private final URI uri;
		
		public StoreAndNotify (final ActorRef sender, final Object message, final ByteStringCachedDocument document, final URI uri) {
			this.sender = sender;
			this.message = message;
			this.document = document;
			this.uri = uri;
		}

		public ActorRef getSender() {
			return sender;
		}

		public Object getMessage() {
			return message;
		}
		
		public ByteStringCachedDocument getDocument () {
			return document;
		}
		
		public URI getUri () {
			return uri;
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
		private final Document document;
		
		public StoreDocument (final Document document) {
			this.document = document;
		}
		
		public Document getDocument () {
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
		private final Document document;
		
		public DocumentStored (final Document document) {
			this.document = document;
		}
		
		public Document getDocument () {
			return document;
		}
	}
	
	private final static class ByteStringCachedDocument implements Serializable {
		private static final long serialVersionUID = -4357319155686482230L;
		
		private final URI uri;
		private final String contentType;
		private final CompactByteString body;
		
		public ByteStringCachedDocument (final URI uri, final String contentType, final CompactByteString body) {
			this.uri = uri;
			this.contentType = contentType;
			this.body = body;
		}
		
		public URI getUri () {
			return uri;
		}

		public String getContentType () {
			return contentType;
		}

		public CompactByteString getBody () {
			return body;
		}
	}
}
