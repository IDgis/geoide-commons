package nl.idgis.geoide.service.actors;

import static akka.pattern.Patterns.ask;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import akka.actor.ActorRef;
import akka.util.ByteString;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.service.Capabilities;
import nl.idgis.geoide.commons.domain.service.messages.ServiceError;
import nl.idgis.geoide.commons.domain.service.messages.ServiceErrorType;
import nl.idgis.geoide.commons.domain.service.messages.ServiceMessage;
import nl.idgis.geoide.commons.domain.service.messages.ServiceMessageContext;
import nl.idgis.geoide.service.messages.GetServiceCapabilities;
import nl.idgis.geoide.service.messages.OGCServiceRequest;
import nl.idgis.geoide.service.messages.OGCServiceResponse;
import nl.idgis.geoide.service.messages.ServiceCapabilities;
import nl.idgis.services.OGCCapabilities;
import nl.idgis.services.OGCCapabilities.Operation;
import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public abstract class OGCService extends Service {

	private final CachedReference<Promise<ServiceMessage>> serviceCapabilities = new CachedReference<> ();
	private final XMLInputFactory xmlInputFactory;
	
	public OGCService (final ActorRef serviceManager, final WSClient wsClient, final ServiceIdentification identification, final long cacheLifetime, final int capabilitiesTimeout, final int requestTimeout) {
		super (serviceManager, wsClient, identification, cacheLifetime, capabilitiesTimeout, requestTimeout);
		
		xmlInputFactory = XMLInputFactory.newInstance ();
		xmlInputFactory.setProperty (XMLInputFactory.SUPPORT_DTD, false);
		xmlInputFactory.setProperty (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
	}

	@Override
	protected boolean handleServiceMessage (final ServiceMessage message) throws Throwable {
		if (message instanceof GetServiceCapabilities) {
			handleGetServiceCapabilities ((GetServiceCapabilities) message);
			return true;
		} else if (message instanceof OGCServiceRequest) {
			handleServiceRequest ((OGCServiceRequest) message);
			return true;
		}
		
		return false;
	}
	
	private void handleServiceRequest (final OGCServiceRequest request) {
		final ActorRef self = self ();
		final ActorRef sender = sender ();
		
		final Promise<Capabilities> capabilitiesPromise = Promise.wrap (ask (serviceManager (), Service.getServiceCapabilities (identification ()), 10000)).map (
				new Function<Object, Capabilities> () {
					@Override
					public Capabilities apply (final Object message) throws Throwable {
						if (message instanceof ServiceError) {
							final ServiceError originalError = (ServiceError)message;
							sender.tell (
									new ServiceError (
											originalError.serviceIdentification (), 
											originalError.errorType (), 
											originalError.url (), 
											originalError.message (),
											originalError.cause (),
											request.context ()
										), 
									self
								);
							return null;
						} else if (message instanceof ServiceCapabilities) {
							return ((ServiceCapabilities) message).capabilities ();
						} else {
							throw new IllegalArgumentException ("Unknown message type: " + message.getClass ().getCanonicalName ());
						}
					}
				}
			);
		
		capabilitiesPromise.onRedeem (new Callback<Capabilities> () {
			@Override
			public void invoke (final Capabilities capabilities) throws Throwable {
				if (capabilities != null && capabilities instanceof OGCCapabilities) {
					doRequest (request, (OGCCapabilities) capabilities, sender, self);
				}
			}
		});
		
		capabilitiesPromise.onFailure (new Callback<Throwable> () {
			@Override
			public void invoke (final Throwable a) throws Throwable {
				sender.tell (raiseServiceError (sender, self, ServiceErrorType.EXCEPTION, identification ().getServiceEndpoint (), a.getMessage (), a, request.context ()), self);
			}
		});
	}
	
	private void doRequest (final OGCServiceRequest request, final OGCCapabilities capabilities, final ActorRef sender, final ActorRef self) throws Throwable {
		final Operation op = capabilities.operationByName (request.request ());
		if (op == null || op.httpGet () == null) {
			sender.tell (raiseServiceError (sender, self, ServiceErrorType.SERVICE_ERROR, identification ().getServiceEndpoint (), String.format ("Invalid request: %s", request.request ()), null, request.context ()), self);
			return;
		}

		final String endpoint = normalizeEndpoint (op.httpGet ());
		final String url;
		final String query;
		final URI uri;
		
		try {
			uri = new URI (endpoint);
		} catch (URISyntaxException e) {
			sender.tell (raiseServiceError (sender, self, ServiceErrorType.SERVICE_ERROR, endpoint, String.format ("Invalid endpoint: %s", endpoint), null, request.context ()), self);
			return;
		}
		
		// Split the endpoint:
		final int offset = endpoint.indexOf ('?');
		if (offset >= 0) {
			url = endpoint.substring (0, offset);
			query = endpoint.substring (offset + 1);
		} else {
			url = endpoint;
			query = "";
		}
		
		// Create the request holder:
		WSRequest holder = wsClient ().url (url).setRequestTimeout (requestTimeout ());
		
		// Add parameters from the endpoint:
		if (!query.isEmpty ()) {
			for (final NameValuePair nvp: URLEncodedUtils.parse (uri, "UTF-8")) {
				holder = holder.setQueryParameter (nvp.getName (), nvp.getValue ());
			}
		}

		// Add standard service parameters:
		holder = holder
				.setQueryParameter ("SERVICE", request.serviceIdentification ().getServiceType ().toUpperCase ())
				.setQueryParameter ("VERSION", request.serviceIdentification ().getServiceVersion ())
				.setQueryParameter ("REQUEST", op.operationType ().operationName ());
		
		// Add parameters from the request object:
		for (final Map.Entry<String, String[]> entry: request.parameters ().entrySet ()) {
			for (final String value: entry.getValue ()) {
				holder = holder.setQueryParameter (entry.getKey (), value);
			}
		}
		
		// Perform the request:
		final Promise<ServiceMessage> promise = get (holder, sender, self, new ResponseHandler () {
			@Override
			public ServiceMessage handleResponse (final WSResponse response, final String url, final ActorRef sender, final ActorRef self) {
				Logger.debug ("Received response " + response.getStatus () + " " + url);
				return handleServiceResponse (request, capabilities, response, url, sender, self);
			}
		}, request.context ());
		
		promise.onRedeem (new Callback<ServiceMessage> () {
			@Override
			public void invoke (final ServiceMessage a) throws Throwable {
				sender.tell (a, self);
			}
		});
		promise.onFailure (new Callback<Throwable> () {
			@Override
			public void invoke(Throwable a) throws Throwable {
				sender.tell (raiseServiceError (sender, self, ServiceErrorType.EXCEPTION, url, a.getMessage (), a, request.context ()), self);
			}
		});
	}
	
	private ServiceMessage handleServiceResponse (final OGCServiceRequest request, final OGCCapabilities capabilities, final WSResponse response, final String url, final ActorRef sender, final ActorRef self) {
		final String contentType = response.getHeader ("Content-Type");
		final ByteString data = ByteString.fromArray (response.asByteArray ());
		final ServiceError exceptionReport = parseServiceExceptionReport (sender, self, url, contentType, data, request.context ()); 
		
		// Return errors:
		if (exceptionReport != null) {
			return exceptionReport;
		}
		
		return new OGCServiceResponse (identification (), capabilities, contentType, data, request.context (), url);
	}
	
	private ServiceError parseServiceExceptionReport (final ActorRef sender, final ActorRef self, final String url, final String contentType, final ByteString data, final ServiceMessageContext context) {
		// Images are never exception reports:
		if (MimeContentType.isValid (contentType)) {
			final MimeContentType mimeType = new MimeContentType (contentType);
			if ("image".equals (mimeType.type ())) {
				return null;
			}
		}

		try {
			final XMLStreamReader reader = xmlInputFactory.createXMLStreamReader (data.iterator ().asInputStream ());
			
			for (int i = 0; reader.hasNext () && i < 20; ++ i) {
				reader.next ();
				
				if (reader.isStartElement() && (reader.getLocalName ().equals ("ServiceExceptionReport") || reader.getLocalName ().equals ("ExceptionReport") || reader.getLocalName ().equals ("Exception") || reader.getLocalName ().equals ("ServiceException"))) {
					final StringBuilder message = new StringBuilder ();
					
					while (!reader.isEndElement ()) {
						if (reader.isCharacters () ) {
							if (message.length () != 0) {
								message.append (" ");
							}
							message.append (reader.getText ());
						}
						reader.next ();
					}
					
					return raiseServiceError (sender, self, ServiceErrorType.SERVICE_ERROR, url, message.toString ().trim (), null, context);
				}
			}
			
			reader.close ();
		} catch (XMLStreamException e) {
			Logger.error ("Error parsing exception report", e);
			return null;
		}
		
		return null;
	}
	
	private static String normalizeEndpoint (final String endpoint) {
		if (endpoint.endsWith ("?") || endpoint.endsWith ("&")) {
			return endpoint.substring (0, endpoint.length () - 1);
		}
		
		return endpoint;
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
		final WSRequest holder = request (identification ())
				.setQueryParameter ("request", "GetCapabilities")
				.setRequestTimeout (capabilitiesTimeout ());
		
		Logger.debug (String.format ("Requesting capabilities for service %s", identification ().toString ()));
		
		return get (holder, sender, self, new ResponseHandler () {
			@Override
			public ServiceMessage handleResponse (final WSResponse response, final String url, final ActorRef sender, final ActorRef self) {
				return parseCapabilities (response, url, sender, self, context);
			}
		}, context);
	}
	
	protected abstract ServiceMessage parseCapabilities (final WSResponse response, final String url, final ActorRef sender, final ActorRef self, final ServiceMessageContext context);
	
	private WSRequest request (final ServiceIdentification identification) {
		return requestForEndpoint (identification.getServiceEndpoint (), capabilitiesTimeout ())
				.setQueryParameter ("service", service ())
				.setQueryParameter ("version", identification.getServiceVersion ());
	}
	
	protected WSRequest requestForEndpoint (final String endpoint, final int timeout) {
		final String url;
		final String query;
		final URI uri;
		
		// Split the endpoint:
		final int offset = endpoint.indexOf ('?');
		if (offset >= 0) {
			url = endpoint.substring (0, offset);
			query = endpoint.substring (offset + 1);
		} else {
			url = endpoint;
			query = "";
		}
		
		// Create the request holder:
		WSRequest holder = wsClient ().url (url).setRequestTimeout (timeout);
		
		// Add parameters from the endpoint:
		if (!query.isEmpty ()) {
			try {
				uri = new URI (endpoint);
				for (final NameValuePair nvp: URLEncodedUtils.parse (uri, "UTF-8")) {
					holder = holder.setQueryParameter (nvp.getName (), nvp.getValue ());
				}
			} catch (URISyntaxException e) {
			}
		}
		
		return holder;
	}

	@Override
	protected void reset() throws Throwable {
		serviceCapabilities.reset ();
	}
	
	protected abstract String service ();
}
