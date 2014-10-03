package nl.idgis.planoview.service.actors;

import static akka.pattern.Patterns.ask;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.planoview.service.messages.GetLayerCapabilities;
import nl.idgis.planoview.service.messages.GetServiceCapabilities;
import nl.idgis.planoview.service.messages.ServiceCapabilities;
import nl.idgis.planoview.service.messages.ServiceControl;
import nl.idgis.planoview.service.messages.ServiceError;
import nl.idgis.planoview.service.messages.ServiceErrorType;
import nl.idgis.planoview.service.messages.ServiceMessage;
import nl.idgis.planoview.service.messages.ServiceMessageContext;
import nl.idgis.planoview.service.messages.ServiceRequest;
import nl.idgis.services.Capabilities;
import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public abstract class Service extends UntypedActor {

	protected final static long DEFAULT_CACHE_LIFETIME = 60 * 5 * 1000;
	protected final static int DEFAULT_CAPABILITIES_TIMEOUT = 5000;
	protected final static int DEFAULT_REQUEST_TIMEOUT = 5000;
	
	private final ActorRef serviceManager;
	private final ServiceIdentification identification;
	
	private final long cacheLifetime;
	private final int capabilitiesTimeout;
	private final int requestTimeout;
	
	public Service (final ActorRef serviceManager, final ServiceIdentification identification, final long cacheLifetime, final int capabilitiesTimeout, final int requestTimeout) {
		if (serviceManager == null) {
			throw new NullPointerException ("serviceManager cannot be null");
		}
		if (identification == null) {
			throw new NullPointerException ("identification cannot be null");
		}
		
		this.serviceManager = serviceManager;
		this.identification = identification;
		this.cacheLifetime = cacheLifetime;
		this.capabilitiesTimeout = capabilitiesTimeout;
		this.requestTimeout = requestTimeout;
	}

	protected ActorRef serviceManager () {
		return serviceManager;
	}
	
	protected ServiceIdentification identification () {
		return identification;
	}
	
	protected long cacheLifetime () {
		return cacheLifetime;
	}
	
	protected int capabilitiesTimeout () {
		return capabilitiesTimeout;
	}
	
	protected int requestTimeout () {
		return requestTimeout;
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (ServiceControl.RESET.equals (message)) {
			try {
				reset ();
			} catch (Throwable t) {
				throw new RuntimeException (t);
			}
		} else if (message instanceof ServiceMessage) {
			try {
				if (!handleServiceMessage ((ServiceMessage) message)) {
					if (message instanceof ServiceRequest) {
						handleServiceRequest ((ServiceRequest) message);
					} else {
						raiseServiceError (sender (), self (), ServiceErrorType.UNSUPPORTED_OPERATION, identification.getServiceEndpoint (), String.format ("Unsupported message type: %s", message.getClass ().getCanonicalName ()), null, ((ServiceMessage)message).context ());
					}
				}
			} catch (Throwable t) {
				raiseServiceError (sender (), self (), ServiceErrorType.EXCEPTION, identification.getServiceEndpoint (), t.getMessage(), t, ((ServiceMessage)message).context ());
			}
		} else {
			unhandled (message);
		}
	}
	
	private void handleServiceRequest (final ServiceRequest request) {
		final ActorRef self = self ();
		final ActorRef sender = sender ();
		
		final Promise<ServiceMessage> responsePromise = Promise.wrap (ask (serviceManager (), Service.getServiceCapabilities (identification ()), 10000)).flatMap (
				new Function<Object, Promise<ServiceMessage>> () {
					@Override
					public Promise<ServiceMessage> apply (final Object message) throws Throwable {
						if (message instanceof ServiceError) {
							return Promise.pure ((ServiceMessage)message);
						} else if (message instanceof ServiceCapabilities) {
							return handleServiceRequest (request, ((ServiceCapabilities)message).capabilities (), sender, self);
						} else {
							throw new IllegalArgumentException ("Unknown message type: " + message.getClass ().getCanonicalName ());
						}
					}
				}
			);

		responsePromise.onRedeem (new Callback<ServiceMessage> () {
			@Override
			public void invoke (final ServiceMessage response) throws Throwable {
				sender.tell (response, self);
			}
		});
		
		responsePromise.onFailure (new Callback<Throwable> () {
			@Override
			public void invoke (final Throwable e) throws Throwable {
				sender.tell (raiseServiceError (sender, self, ServiceErrorType.EXCEPTION, identification ().getServiceEndpoint (), e.getMessage (), e, request.context ()), self);
			}
		});
	}
	
	protected abstract Promise<ServiceMessage> handleServiceRequest (ServiceRequest request, Capabilities capabilities, final ActorRef sender, final ActorRef self) throws Throwable;
	protected abstract boolean handleServiceMessage (ServiceMessage message) throws Throwable;
	protected abstract void reset () throws Throwable;
	
	protected Promise<ServiceMessage> get (final WSRequestHolder requestHolder, final ActorRef sender, final ActorRef self, final ResponseHandler responseHandler, final ServiceMessageContext context) {
		return handleResponse (requestHolder, requestHolder.get (), sender, self, responseHandler, context);
	}
	
	protected Promise<ServiceMessage> post (final WSRequestHolder requestHolder, final InputStream inputStream, final ActorRef sender, final ActorRef self, final ResponseHandler responseHandler, final ServiceMessageContext context) {
		return handleResponse (requestHolder, requestHolder.post (inputStream), sender, self, responseHandler, context);
	}
	
	protected Promise<ServiceMessage> handleResponse (final WSRequestHolder requestHolder, final Promise<WSResponse> promise, final ActorRef sender, final ActorRef self, final ResponseHandler responseHandler, final ServiceMessageContext context) {
		final String url = requestHolderToUrl (requestHolder);
		promise.onFailure (new Callback<Throwable> () {
			@Override
			public void invoke (final Throwable e) throws Throwable {
				sender.tell (raiseServiceError (sender, self, ServiceErrorType.EXCEPTION, url, e.getMessage (), e, context), self);
			}
		});
		
		Logger.debug ("Executing service request: " + url);
		
		return promise.map (new Function<WSResponse, ServiceMessage> () {
			@Override
			public ServiceMessage apply (final WSResponse response) throws Throwable {
				if (response.getStatus () < 200 || response.getStatus () >= 300) {
					// Report a HTTP error:
					return raiseServiceError (
							sender,
							self,
							ServiceErrorType.HTTP_ERROR,
							url,
							String.format ("%d: %s", response.getStatus (), response.getStatusText ()),
							null,
							context
						);
				}
				
				// Invoke the handler:
				return responseHandler.handleResponse (response, url, sender, self);
			}
		});
	}
	
	protected ServiceError raiseServiceError (final ActorRef sender, final ActorRef self, final ServiceErrorType errorType, final String url, final String message, final Throwable cause, final ServiceMessageContext context) {
		final ServiceError error = new ServiceError (identification, errorType, url, message, cause, context);
		
		// Report the error to the service manager, for logging purposes: 
		serviceManager.tell (error, self);
		
		// Log the error:
		Logger.error (String.format ("Service %s raised an error %s with request: %s Message: %s", identification.toString (), errorType.toString (), url, message == null ? "-" : message), cause);
		
		return error;
	}
	
	protected static void sendReset (final ActorRef actor) {
		actor.tell (ServiceControl.RESET, actor);
	}
	
	protected static String requestHolderToUrl (final WSRequestHolder holder) {
		final StringBuilder parameters = new StringBuilder ();
		final Map<String, Collection<String>> queryParameters = holder.getQueryParameters ();
		
		if (queryParameters != null && !queryParameters.isEmpty ()) {
			for (final Map.Entry<String, Collection<String>> entry: queryParameters.entrySet ()) {
				final Collection<String> values = entry.getValue ();
				if (values != null && !values.isEmpty ()) {
					for (final String value: values) {
						if (parameters.length () > 0) {
							parameters.append ("&");
						}
						try {
							parameters.append (URLEncoder.encode (entry.getKey (), "UTF-8"));
							parameters.append ("=");
							parameters.append (URLEncoder.encode (value, "UTF-8"));
						} catch (UnsupportedEncodingException e) {
						}
					}
				}
			}
		}
		
		return holder.getUrl () + (parameters.length () > 0 ? "?" + parameters.toString () : "");
	}
	
	public static ServiceIdentification identification (final String endpoint, final String version, final String type) {
		return new ServiceIdentification (endpoint, version, type);
	}
	
	public static GetServiceCapabilities getServiceCapabilities (final ServiceIdentification identification) {
		return new GetServiceCapabilities (identification);
	}

	public static GetLayerCapabilities getLayerCapabilities (final ServiceIdentification identification, final Capabilities.Layer layer) {
		return getLayerCapabilities (identification, layer);
	}

	public static interface Factory {
		Props mkProps (ActorRef serviceManager, ServiceIdentification identification);
	}
	
	protected static interface ResponseHandler {
		ServiceMessage handleResponse (final WSResponse response, final String url, final ActorRef sender, final ActorRef self);
	}
}
