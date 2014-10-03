package nl.idgis.planoview.service.tms.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.service.actors.CachedReference;
import nl.idgis.geoide.service.actors.Service;
import nl.idgis.geoide.service.messages.GetServiceCapabilities;
import nl.idgis.geoide.service.messages.ServiceCapabilities;
import nl.idgis.geoide.service.messages.ServiceError;
import nl.idgis.geoide.service.messages.ServiceErrorType;
import nl.idgis.geoide.service.messages.ServiceMessage;
import nl.idgis.geoide.service.messages.ServiceMessageContext;
import nl.idgis.geoide.service.messages.ServiceRequest;
import nl.idgis.geoide.service.messages.ServiceResponse;
import nl.idgis.services.Capabilities;
import nl.idgis.services.client.tms.TMSCapabilitiesParser;
import nl.idgis.services.client.tms.TMSCapabilitiesParser.ParseException;
import nl.idgis.services.tms.TMSCapabilities;
import nl.idgis.services.tms.TMSCapabilities.TileMapLayer;
import nl.idgis.services.tms.TMSCapabilities.TileSet;
import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.util.ByteString;

import com.google.common.collect.TreeMultimap;

public class TMS extends Service {

	private final CachedReference<Promise<ServiceMessage>> serviceCapabilities = new CachedReference<> ();
	private final ConcurrentMap<String, CachedReference<Promise<TileMapLayer>>> tileMapLayerCapabilities = new ConcurrentHashMap<> ();
	
	public TMS (final ActorRef serviceManager, final ServiceIdentification identification) {
		super(serviceManager, identification, DEFAULT_CACHE_LIFETIME, DEFAULT_CAPABILITIES_TIMEOUT, DEFAULT_REQUEST_TIMEOUT);
	}
	
	public static Props mkProps (final ActorRef serviceManager, final ServiceIdentification identification) {
		return Props.create (TMS.class, serviceManager, identification);
	}

	@Override
	protected boolean handleServiceMessage (final ServiceMessage message) throws Throwable {
		if (message instanceof GetServiceCapabilities) {
			handleGetServiceCapabilities ((GetServiceCapabilities)message);
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void reset () {
		serviceCapabilities.reset ();
	}
	
	@Override
	protected Promise<ServiceMessage> handleServiceRequest (final ServiceRequest request, final Capabilities capabilities, final ActorRef sender, final ActorRef self) throws Throwable {
		final TMSCapabilities.TileMapService tms = (TMSCapabilities.TileMapService) capabilities;
		final TMSCapabilities.Layer layer = (TMSCapabilities.Layer) tms.layer (request.layerName ());
		
		if (layer == null) {
			return Promise.<ServiceMessage>pure (raiseServiceError (sender, self, ServiceErrorType.SERVICE_ERROR, identification ().getServiceEndpoint (), String.format ("Layer %s not found", request.layerName ()), null, request.context ()));
		}
		
		// Parse the path (srs/z/x/y.ext):
		final String[] parts = request.path ().split ("\\/");
		if (parts.length != 4) {
			return Promise.<ServiceMessage>pure (raiseServiceError (sender, self, ServiceErrorType.SERVICE_ERROR, identification ().getServiceEndpoint (), String.format ("Invalid path %s", request.path ()), null, request.context ()));
		}
		final int offset = parts[3].indexOf ('.');
		final String srs = parts[0];
		final String z = parts[1];
		final String x = parts[2];
		final String y = offset > 0 ? parts[3].substring (0, offset) : parts[3];
		final String ext = offset > 0 ? parts[3].substring (offset + 1).toLowerCase () : "png";
		
		// Get the capabilities for all candidate tile maps:
		final List<Promise<TileMapLayer>> candidates = new ArrayList<> ();
		for (final TMSCapabilities.TileMap tileMap: layer.tileMaps ()) {
			if (!tileMap.srs().toLowerCase ().equals (srs.toLowerCase ())) {
				continue;
			}
			
			if (tileMap instanceof TileMapLayer) {
				candidates.add (Promise.<TileMapLayer>pure ((TileMapLayer) tileMap));
			} else {
				candidates.add (getTileMapLayer (tileMap, sender, self, request.context ()));
			}
		}
		
		return Promise
			.sequence (candidates)
			.flatMap (new Function<List<TileMapLayer>, Promise<ServiceMessage>> () {
				@Override
				public Promise<ServiceMessage> apply (final List<TileMapLayer> tileMapLayers) throws Throwable {
					final boolean isPng = "png".equals (ext);
					final boolean isJpeg = "jpg".equals (ext) || "jpeg".equals (ext);
					final TreeMultimap<Integer, ComparableTileMapLayer> matchingLayers = TreeMultimap.<Integer, ComparableTileMapLayer>create ();
					
					for (final TileMapLayer tileMapLayer: tileMapLayers) {
						if (tileMapLayer == null) {
							continue;
						}
						
						final int priority;
						if (tileMapLayer.tileFormat ().extension ().toLowerCase ().equals (ext)) {
							priority = 0;
						} else if (isJpeg && tileMapLayer.tileFormat ().extension ().toLowerCase ().startsWith ("jp")) {
							priority = 1;
						} else if (isPng && tileMapLayer.tileFormat ().extension ().toLowerCase ().startsWith ("png")) {
							priority = 1;
						} else {
							priority = 2;
						}
						
						matchingLayers.put (priority, new ComparableTileMapLayer (tileMapLayer));
					}
					
					if (!matchingLayers.isEmpty ()) {
						return doGetTile (matchingLayers.values().iterator ().next ().tileMapLayer, capabilities, request.path (), x, y, z, sender, self, request.context ());
					}
					
					return Promise.<ServiceMessage>pure (raiseServiceError (sender, self, ServiceErrorType.SERVICE_ERROR, identification ().getServiceEndpoint (), String.format ("No matching layer for %s (invalid extension)", request.path ()), null, request.context ()));
				}
			});
	}
	
	private Promise<ServiceMessage> doGetTile (final TileMapLayer tileMapLayer, final Capabilities capabilities, final String path, final String x, final String y, final String z, final ActorRef sender, final ActorRef self, final ServiceMessageContext context) {
		// Locate a tileset:
		TileSet tileSet = null;
		for (final TileSet ts: tileMapLayer.tileSets ()) {
			if (z.equals (Integer.toString (ts.order ()))) {
				tileSet = ts;
				break;
			}
		}
		if (tileSet == null) {
			return Promise.<ServiceMessage>pure (raiseServiceError (sender, self, ServiceErrorType.SERVICE_ERROR, identification ().getServiceEndpoint (), String.format ("No matching tileset for %s", path), null, context));
		}
		
		// Construct the url:
		final String url = tileSet.href ().endsWith ("/") ? tileSet.href () : tileSet.href () + "/";
		
		final WSRequestHolder holder = WS
				.url (url + x + "/" + y + "." + tileMapLayer.tileFormat ().extension ())
				.setTimeout (requestTimeout ());
		
		return get (
			holder, 
			sender, 
			self, 
			new ResponseHandler () {
				@Override
				public ServiceMessage handleResponse (final WSResponse response, final String url, final ActorRef sender, final ActorRef self) {
					if (response.getStatus () != 200) {
						return raiseServiceError (sender, self, ServiceErrorType.SERVICE_ERROR, url, response.getStatusText (), null, context);
					}
					
					return new ServiceResponse (
							identification (), 
							capabilities, 
							url, 
							response.getHeader ("Content-Type"), 
							getCacheHeaders (response), 
							ByteString.fromArray (response.asByteArray ())
						);
				}
			}, 
			context
		);
	}
	
	private Map<String, String> getCacheHeaders (final WSResponse response) {
		final Map<String, String> headers = new HashMap<> ();
		
		getCacheHeader (response, headers, "Cache-Control");
		getCacheHeader (response, headers, "Age");
		getCacheHeader (response, headers, "Date");
		getCacheHeader (response, headers, "ETag");
		getCacheHeader (response, headers, "Expires");
		getCacheHeader (response, headers, "Last-Modified");
		getCacheHeader (response, headers, "Retry-After");
		
		return headers;
	}
	
	private void getCacheHeader (final WSResponse response, final Map<String, String> headers, final String name) {
		final String value = response.getHeader (name);
		if (value != null) {
			headers.put (name, value);
		}
	}
	
	private synchronized Promise<TileMapLayer> getTileMapLayer (final TMSCapabilities.TileMap tileMap, final ActorRef sender, final ActorRef self, final ServiceMessageContext context) throws Throwable {
		final String name = String.format ("%s/%s/%s", tileMap.title (), tileMap.srs (), tileMap.profile ());
		
		final CachedReference<Promise<TileMapLayer>> newPromiseReference = new CachedReference<Promise<TileMapLayer>> ();
		final CachedReference<Promise<TileMapLayer>> previousPromiseReference = tileMapLayerCapabilities.putIfAbsent (name, newPromiseReference);
		final CachedReference<Promise<TileMapLayer>> promiseReference = previousPromiseReference != null ? previousPromiseReference : newPromiseReference;
		
		return promiseReference.get (new Function0<Promise<TileMapLayer>> () {
			@Override
			public Promise<TileMapLayer> apply () throws Throwable {
				return doGetTileMapLayer (tileMap, sender, self, context);
			}
		}, cacheLifetime ());
	}
	
	private Promise<TileMapLayer> doGetTileMapLayer (final TMSCapabilities.TileMap tileMap, final ActorRef sender, final ActorRef self, final ServiceMessageContext context) throws Throwable {
		Logger.debug ("Requesting tilemap: " + tileMap.href ());
		
		return WS
			.url (tileMap.href ())
			.setTimeout (requestTimeout ())
			.get ()
			.map (new Function<WSResponse, TileMapLayer> () {
				@Override
				public TileMapLayer apply (final WSResponse response) throws Throwable {
					if (response.getStatus () != 200) {
						return null;
					}
					
					return TMSCapabilitiesParser.parseTileMapCapabilities (tileMap.href (), response.getBodyAsStream ());
				}
			});
	}
	
	private void handleGetServiceCapabilities (final GetServiceCapabilities message) throws Throwable {
		final ActorRef self = self ();
		final ActorRef sender = sender ();
		
		final Promise<ServiceMessage> capabilitiesPromise = serviceCapabilities.get (new Function0<Promise<ServiceMessage>> () {
			@Override
			public Promise<ServiceMessage> apply () throws Throwable {
				return doGetCapabilities (self, sender, message.context ());
			}
		}, cacheLifetime ());
		
		capabilitiesPromise.onRedeem (new Callback<ServiceMessage>() {
			@Override
			public void invoke (final ServiceMessage capabilities) throws Throwable {
				sender.tell (capabilities, self);
				
				// Reset the cached capabilities when an error occurred to retry the failed request:
				if (capabilities instanceof ServiceError) {
					sendReset (self);
				}
			}
		});
	}
	
	private Promise<ServiceMessage> doGetCapabilities (final ActorRef self, final ActorRef sender, final ServiceMessageContext context) {
		final WSRequestHolder holder = WS
				.url (identification ().getServiceEndpoint ())
				.setTimeout (capabilitiesTimeout ());
		
		Logger.debug (String.format ("Requesting capabilities for service %s", identification ().toString ()));
		
		return get (holder, sender, self, new ResponseHandler () {
			@Override
			public ServiceMessage handleResponse (final WSResponse response, final String url, final ActorRef sender, final ActorRef self) {
				return parseCapabilities (response, url, sender, self, context);
			}
		}, context);
	}
	
	private ServiceMessage parseCapabilities (final WSResponse response, final String url, final ActorRef sender, final ActorRef self, final ServiceMessageContext context) {
		Logger.debug (String.format ("Parsing capabilities document for service %s", identification ().toString ()));
		
		final TMSCapabilities.TileMapService capabilities;
		
		try {
			capabilities = TMSCapabilitiesParser.parseCapabilities (url, response.getBodyAsStream ());
		} catch (ParseException e) {
			return raiseServiceError (sender, self, ServiceErrorType.FORMAT_ERROR, url, e.getMessage (), e, context);
		}
		
		return new ServiceCapabilities (identification (), capabilities);
	}
	
	private static class ComparableTileMapLayer implements Comparable<ComparableTileMapLayer> {
		
		public final TileMapLayer tileMapLayer;
		
		public ComparableTileMapLayer (final TileMapLayer layer) {
			this.tileMapLayer = layer;
		}

		@Override
		public int compareTo (final ComparableTileMapLayer o) {
			return tileMapLayer.title ().compareTo (o.tileMapLayer.title ());
		}
	}
}
