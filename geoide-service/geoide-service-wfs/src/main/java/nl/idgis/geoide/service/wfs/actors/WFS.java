package nl.idgis.geoide.service.wfs.actors;

import java.util.Map;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.QName;
import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.service.actors.OGCService;
import nl.idgis.geoide.service.messages.GetServiceCapabilities;
import nl.idgis.geoide.service.messages.QueryFeatures;
import nl.idgis.geoide.service.messages.QueryFeaturesResponse;
import nl.idgis.geoide.service.messages.ServiceCapabilities;
import nl.idgis.geoide.service.messages.ServiceError;
import nl.idgis.geoide.service.messages.ServiceErrorType;
import nl.idgis.geoide.service.messages.ServiceMessage;
import nl.idgis.geoide.service.messages.ServiceMessageContext;
import nl.idgis.geoide.service.messages.ServiceRequest;
import nl.idgis.geoide.service.wfs.WFSRequestParameters;
import nl.idgis.ogc.client.wfs.WFSCapabilitiesParser;
import nl.idgis.ogc.client.wfs.WFSCapabilitiesParser.ParseException;
import nl.idgis.ogc.wfs.WFSCapabilities;
import nl.idgis.services.Capabilities;
import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;

public class WFS extends OGCService {

	public WFS (final ActorRef serviceManager, final WSClient wsClient, final ServiceIdentification identification) {
		super(serviceManager, wsClient, identification, DEFAULT_CACHE_LIFETIME, DEFAULT_CAPABILITIES_TIMEOUT, DEFAULT_REQUEST_TIMEOUT);
	}
	
	public static Props mkProps (final ActorRef serviceManager, final WSClient wsClient, final ServiceIdentification identification) {
		return Props.create (WFS.class, serviceManager, wsClient, identification);
	}

	@Override
	protected boolean handleServiceMessage (final ServiceMessage message) throws Throwable {
		if (message instanceof QueryFeatures) {
			handleQueryFeatures ((QueryFeatures) message);
		}
		
		return super.handleServiceMessage (message);
	}
	
	@Override
	protected String service () {
		return "WFS";
	}

	@Override
	protected ServiceMessage parseCapabilities (final WSResponse response, final String url, final ActorRef sender, final ActorRef self, final ServiceMessageContext context) {
		Logger.debug (String.format ("Parsing capabilities document for service %s", identification ().toString ()));
		
		final WFSCapabilities capabilities;
		
		try {
			capabilities = WFSCapabilitiesParser.parseCapabilities (response.getBodyAsStream ());
		} catch (ParseException e) {
			return raiseServiceError (sender, self, ServiceErrorType.FORMAT_ERROR, url, e.getMessage (), e, context);
		}
		
		return new ServiceCapabilities (identification (), capabilities);
	}
	
	private void handleQueryFeatures (final QueryFeatures queryFeatures) {
		final ActorRef sender = sender ();
		final ActorRef self = self ();
		final ActorRef streamer = context ().actorOf (FeatureCollectionStreamer.props ());
		
		withCapabilities (self (), sender (), new Callback<WFSCapabilities> () {
			@Override
			public void invoke (final WFSCapabilities capabilities) throws Throwable {
				Logger.debug ("Got capabilities for service: " + capabilities.serviceIdentification ().title ());
				
				// Locate the feature type:
				final WFSCapabilities.FeatureType wfsFeatureType = findFeatureType (
						capabilities, 
						queryFeatures
							.getFeatureType ()
							.getFeatureType ()
							.getName ()
					);
				if (wfsFeatureType == null) {
					sender.tell (raiseServiceError (
							sender, 
							self, 
							ServiceErrorType.UNSUPPORTED_OPERATION, 
							identification ().getServiceEndpoint (), 
							"Unknown feature type", 
							null, 
							null
						), self);
					return;
				}
				
				// Determine the output format:
				final MimeContentType outputFormat = getOutputFormat (wfsFeatureType);
				if (outputFormat == null) {
					sender.tell (raiseServiceError (
							sender, self, ServiceErrorType.UNSUPPORTED_OPERATION,
							identification ().getServiceEndpoint (),
							"No valid output format for " + wfsFeatureType.namespacePrefix () + ":" + wfsFeatureType.name (),
							null,
							null
						), self);
					return;
				}

				Logger.debug ("Querying " + wfsFeatureType.namespacePrefix () + ":" + wfsFeatureType.name () + " " + outputFormat);
				
				// Construct a request:
				WSRequestHolder holder = requestForEndpoint (identification ().getServiceEndpoint (), 20000)
					.setQueryParameter ("service", "WFS")
					.setQueryParameter ("version", identification ().getServiceVersion ())
					.setQueryParameter ("request", "GetFeature")
					.setQueryParameter ("outputformat", outputFormat.original ());
					
				if (wfsFeatureType.namespacePrefix () != null) {
					holder = holder.setQueryParameter ("typename", wfsFeatureType.namespacePrefix () + ":" + wfsFeatureType.name ());
					if (wfsFeatureType.namespaceUri () != null) {
						holder = holder.setQueryParameter ("namespace", String.format ("xmlns(%s=%s)", wfsFeatureType.namespacePrefix (), wfsFeatureType.namespaceUri ()));
					}
				} else {
					holder = holder.setQueryParameter ("typename", wfsFeatureType.name ());
				}
				
				// Add vendor parameters:
				if (queryFeatures.getFeatureType ().getParameters () != null && queryFeatures.getFeatureType ().getParameters () instanceof WFSRequestParameters) {
					final WFSRequestParameters requestParameters = (WFSRequestParameters) queryFeatures.getFeatureType ().getParameters ();
					
					for (final Map.Entry<String, String> entry: requestParameters.getVendorParameters ().entrySet ()) {
						holder = holder.setQueryParameter (entry.getKey (), entry.getValue ());
					}
				}
					
				final Promise<ServiceMessage> responsePromise = get (holder, sender, self, new ResponseHandler () {
					@Override
					public ServiceMessage handleResponse (final WSResponse response, final String url, final ActorRef sender, final ActorRef self) {
						Logger.debug ("Got WFS response");
						
						// Tell the streamer to consume the stream:
						streamer.tell (new FeatureCollectionStreamer.ConsumeStream (outputFormat, response.getBodyAsStream ()), self);
						
						// Inform the sender of the reference to the stream actor to consume:
						return new QueryFeaturesResponse (identification (), streamer);
					}
				}, queryFeatures.context ());
				
				responsePromise.onRedeem (new Callback<ServiceMessage> () {

					@Override
					public void invoke (final ServiceMessage msg) throws Throwable {
						if (msg != null) {
							sender.tell (msg, self);
						}
					}
				});
			}
		});
	}
	
	private WFSCapabilities.FeatureType findFeatureType (final WFSCapabilities capabilities, final QName name) {
		for (final WFSCapabilities.FeatureType ft: capabilities.featureTypes ()) {
			if (!name.getLocalName ().equals (ft.name ())) {
				continue;
			}
			
			if (name.getNamespace () != null && ft.namespaceUri () != null && !name.getNamespace ().equals (ft.namespaceUri ())) {
				continue;
			}
			
			return ft;
		}
		
		return null;
	}
	
	private void withCapabilities (final ActorRef self, final ActorRef sender, final Callback<WFSCapabilities> callback) {
		final Promise<Object> capabilitiesPromise = Promise.wrap (
				Patterns.ask (
						self (), 
						new GetServiceCapabilities (identification ()), 
						10000
				)
			);
		
		capabilitiesPromise.onRedeem (new Callback<Object> () {
			@Override
			public void invoke (final Object capabilitiesOrError) throws Throwable {
				if (capabilitiesOrError instanceof ServiceCapabilities) {
					callback.invoke ((WFSCapabilities) ((ServiceCapabilities) capabilitiesOrError).capabilities ());
				} else if (capabilitiesOrError instanceof ServiceError) {
					sender.tell (capabilitiesOrError, self);
				} else {
					throw new IllegalArgumentException ("Unknown message type: " + capabilitiesOrError.getClass ().getCanonicalName ());
				}
			}
		});
	}
	
	private static MimeContentType getOutputFormat (final WFSCapabilities.FeatureType ft) {
		String highestVersion = "0.0.0";
		MimeContentType outputFormat = null;
		
		for (final MimeContentType contentType: ft.outputFormats ()) {
			for (final Map.Entry<String, String> entry: contentType.parameters ().entrySet ()) {
				if (!"subtype".equals (entry.getKey ().toLowerCase ())) {
					continue;
				}
				if (!entry.getValue ().startsWith ("gml/")) {
					continue;
				}
				
				final String version = entry.getValue ().substring (4);
				
				if ((version.startsWith ("2") || version.startsWith ("3.0") || version.startsWith ("3.1") || version.startsWith ("3.2")) && version.compareTo (highestVersion) > 0 ) {
					highestVersion = version;
					outputFormat = contentType;
					break;
				}
			}
		}
		
		return outputFormat;
	}
	
	/*
	private void handleGetFeature (final GetFeature getFeature) {
		Logger.debug ("Requesting feature: " + getFeature.featureId ());
		final ActorRef self = self ();
		final ActorRef sender = sender ();
		
		// Determine the GML version:
		final MimeContentType outputFormat = getOutputFormat (getFeature.featureType ());
		if (outputFormat == null) {
			sender.tell (raiseServiceError (sender, self, ServiceErrorType.FORMAT_ERROR, getFeature.serviceIdentification ().getServiceEndpoint (), "No supported output format could be found", null, getFeature.context ()), self);
			return;
		}
		
		// Create the request:
		final Map<String, String[]> parameters = new HashMap<> ();

		if (getFeature.featureType ().namespacePrefix () != null) {
			parameters.put ("TYPENAME", new String[] { getFeature.featureType ().namespacePrefix () + ":" + getFeature.featureType ().name () });
			if (getFeature.featureType ().namespaceUri () != null) {
				parameters.put ("NAMESPACE", new String[] { String.format ("xmlns(%s=%s)", getFeature.featureType.namespacePrefix (), getFeature.featureType.namespaceUri ()) });
			}
		} else {
			parameters.put ("TYPENAME", new String[] { getFeature.featureType ().name () });
		}
		parameters.put ("FEATUREID", new String[] { getFeature.featureId () });
		parameters.put ("OUTPUTFORMAT", new String[] { outputFormat.original () });
		
		final OGCServiceRequest request = new OGCServiceRequest (getFeature.serviceIdentification (), "GetFeature", parameters);
		
		final Promise<OGCServiceResponse> responsePromise = Promise.wrap (ask (serviceManager (), request, 10000)).map (
			new Function<Object, OGCServiceResponse> () {
				@Override
				public OGCServiceResponse apply (final Object response) throws Throwable {
					if (response instanceof ServiceError) {
						sender.tell (response, self);
						return null;
					} else if (response instanceof OGCServiceResponse) {
						return (OGCServiceResponse) response;
					} else {
						throw new IllegalArgumentException (String.format ("Unexpected message type: %s", response.getClass ().getCanonicalName ()));
					}
				}
			}
		);
		
		// Handle failure:
		responsePromise.onFailure (new Callback<Throwable> () {
			@Override
			public void invoke (final Throwable a) throws Throwable {
				sender.tell (raiseServiceError(sender, self, ServiceErrorType.EXCEPTION, getFeature.serviceIdentification ().getServiceEndpoint (), a.getMessage (), a, getFeature.context ()), self);
			}
		});
		
		// Handle the response:
		responsePromise.onRedeem (new Callback<OGCServiceResponse> () {
			@Override
			public void invoke (final OGCServiceResponse response) throws Throwable {
				if (response == null) {
					return;
				}
				
				try {
					final FeatureCollectionReader featureCollectionReader = new FeatureCollectionReader (outputFormat);
					try (final InputStream is = response.data ().iterator ().asInputStream ()) {
						final FeatureCollection featureCollection = featureCollectionReader.parseCollection (is);
						Feature firstFeature = null;
						
						for (final Feature feature: featureCollection) {
							if (firstFeature != null) {
								sender.tell (raiseServiceError(sender, self, ServiceErrorType.FORMAT_ERROR, getFeature.serviceIdentification ().getServiceEndpoint (), "GetFeatureById returned multiple features", null, getFeature.context ()), self);
								return;
							}
							firstFeature = feature;
						}
						
						sender.tell (new FeatureResponse (getFeature.serviceIdentification (), firstFeature), self);
					}
				} catch (FeatureCollectionReader.ParseException e) {
					sender.tell (raiseServiceError(sender, self, ServiceErrorType.FORMAT_ERROR, getFeature.serviceIdentification ().getServiceEndpoint (), e.getMessage (), e, getFeature.context ()), self);
				}
			}
		});
	}
	*/
	
	/*
	public final static class GetFeature extends ServiceMessage {
		private static final long serialVersionUID = 7435831319479240882L;
		
		private final String featureId;
		private final WFSCapabilities.FeatureType featureType;
		
		public GetFeature (final ServiceIdentification serviceIdentification, final WFSCapabilities.FeatureType featureType, final String featureId) {
			super(serviceIdentification);

			if (featureType == null) {
				throw new NullPointerException ("featureType cannot be null");
			}
			if (featureId == null) {
				throw new NullPointerException ("featureId cannot be null");
			}

			this.featureType = featureType; 
			this.featureId = featureId;
		}
		
		public WFSCapabilities.FeatureType featureType () {
			return this.featureType;
		}
		
		public String featureId () {
			return this.featureId;
		}
	}
	
	public final static class FeatureResponse extends ServiceMessage {
		private static final long serialVersionUID = 5783736709916538306L;

		private final Feature feature;
		
		public FeatureResponse (final ServiceIdentification serviceIdentification, final Feature feature) {
			super(serviceIdentification);
			
			this.feature = feature;
		}
		
		public boolean hasFeature () {
			return feature != null;
		}
		
		public Feature feature () {
			return feature;
		}
	}
	*/

	@Override
	protected Promise<ServiceMessage> handleServiceRequest(
			ServiceRequest request, Capabilities capabilities, ActorRef sender,
			ActorRef self) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
}