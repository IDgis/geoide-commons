package nl.idgis.planoview.service.actors;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.idgis.planoview.commons.domain.ServiceIdentification;
import nl.idgis.planoview.service.ServiceType;
import nl.idgis.planoview.service.ServiceTypeRegistry;
import nl.idgis.planoview.service.messages.LogResponse;
import nl.idgis.planoview.service.messages.RequestLog;
import nl.idgis.planoview.service.messages.ServiceError;
import nl.idgis.planoview.service.messages.ServiceMessage;
import play.Logger;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

public class ServiceManager extends UntypedActor {

	private final static int SERVICE_DESTROY_DURATION = 10 * 60;
	private final static int MAX_LOG_LENGTH = 1000;
	
	private final ServiceTypeRegistry serviceTypeRegistry;
	
	private final Map<ActorRef, ServiceIdentification> actorRefs = new HashMap<> ();
	private final Map<ServiceIdentification, ActorRef> serviceIdentifications = new HashMap<> ();
	private final Map<ServiceIdentification, Cancellable> serviceTerminationSchedule = new HashMap<> ();
	
	private final LinkedList<ServiceError> errorMessages = new LinkedList<> ();

	// public final static ActorRef instance = Akka.system ().actorOf (ServiceManager.mkPropsDefault ());

	public ServiceManager (final ServiceTypeRegistry serviceTypeRegistry) {
		if (serviceTypeRegistry == null) {
			throw new NullPointerException ("serviceTypeRegistry cannot be null");
		}
		
		this.serviceTypeRegistry = serviceTypeRegistry;
	}
	
	public static Props mkProps (final ServiceTypeRegistry serviceTypeRegistry) {
		return Props.create (ServiceManager.class, serviceTypeRegistry);
	}
	
	@Override
	public void onReceive (final Object msg) throws Exception {

		if (msg instanceof DestroyService) {
			final DestroyService destroyService = (DestroyService)msg;
			
			// Terminate the actor:
			if (serviceIdentifications.containsKey (destroyService.serviceIdentification ())) {
				getContext ()
					.system ()
					.stop (
						serviceIdentifications
							.get (destroyService.serviceIdentification ())
					);
			}
		} else if (msg instanceof Terminated) {
			handleTermination (((Terminated)msg).actor ());
		} else if (msg instanceof RequestLog) {
			handleRequestLog ((RequestLog) msg);
		} else if (msg instanceof ServiceError) {
			handleServiceError ((ServiceError) msg);
		} else if (msg instanceof ServiceMessage) {
			handleServiceMessage ((ServiceMessage)msg);
		} else {
			unhandled (msg);
		}
	}

	private void handleServiceError (final ServiceError error) {
		while (errorMessages.size () >= MAX_LOG_LENGTH) {
			errorMessages.removeLast ();
		}
		
		errorMessages.addFirst (error);
	}

	private void handleRequestLog (final RequestLog requestLog) {
		final List<ServiceError> messages = new ArrayList<> ();
		
		if (requestLog.count () > 0 && requestLog.start () < errorMessages.size ()) {
			for (int i = requestLog.start (); i < requestLog.start () + requestLog.count () && i < errorMessages.size (); ++ i) {
				messages.add (errorMessages.get (i));
			}
		}
		
		sender ().tell (new LogResponse (messages, requestLog.start (), errorMessages.size ()), self ());
	}
	
	private void handleTermination (final ActorRef ref) {
		// Remove the service from the pool:
		removeService (ref);
	}
	
	private void handleServiceMessage (final ServiceMessage message) throws Exception {
		final ServiceIdentification identification = message.serviceIdentification ();
		final ActorRef ref;
		
		// Locate or create the actor:
		if (serviceIdentifications.containsKey (identification)) {
			ref = serviceIdentifications.get (identification);
		} else {
			ref = createServiceActor (identification);
		}
		
		// Dispatch the message:
		ref.tell (message, sender ());

		// Update the termination schedule for the actor:
		scheduleServiceTermination (message.serviceIdentification ());
	}
	
	private ActorRef createServiceActor (final ServiceIdentification identification) throws Exception {
		// Locate the factory for this service type:
		final ServiceType serviceType = serviceTypeRegistry.getServiceType (identification.getServiceType ());
		if (serviceType == null) {
			throw new IllegalArgumentException (String.format ("No service type defined for %s", identification.getServiceType ()));
		}
		
		Logger.debug (String.format ("Creating actor for service %s", identification.toString ()));
		
		// Create an actor with a unique name:
		final String name = String.format (
				"service-%s-%s-%s", 
				identification.getServiceType ().toLowerCase (), 
				URLEncoder.encode (identification.getServiceVersion (), "UTF-8"), 
				URLEncoder.encode (identification.getServiceEndpoint (), "UTF-8")
			);
		final ActorRef ref = getContext ().actorOf (serviceType.createServiceActorProps (self (), identification), name);
		
		addService (identification, ref);
		scheduleServiceTermination (identification);
		
		// Watch the actor:
		getContext ().watch (ref);
		
		return ref;
	}
	
	private void scheduleServiceTermination (final ServiceIdentification identification) {
		// Cancel the previous schedule:
		if (serviceTerminationSchedule.containsKey (identification)) {
			serviceTerminationSchedule
				.remove (identification)
				.cancel ();
		}

		// Create a schedule that will destroy this actor in the future:
		final Cancellable cancellable = getContext ()
			.system ()
			.scheduler ()
			.scheduleOnce (
					Duration.create (SERVICE_DESTROY_DURATION, TimeUnit.SECONDS), 
					self (), 
					new DestroyService (identification), 
					getContext ().system ().dispatcher (), 
					self ()
				);
		
		// Store the cancellable so that the schedule can be cancelled if the service is used in the future:
		serviceTerminationSchedule.put (identification, cancellable);
	}
	
	private void addService (final ServiceIdentification identification, final ActorRef ref) {
		if (serviceIdentifications.containsKey (identification)) {
			throw new IllegalStateException ("An actor for service " + identification + " already exists.");
		}
		if (actorRefs.containsKey (ref)) {
			throw new IllegalStateException ("The actor " + ref + " is already registered.");
		}
		
		actorRefs.put (ref, identification);
		serviceIdentifications.put (identification, ref);
	}
	
	private void removeService (final ServiceIdentification identification) {
		if (!serviceIdentifications.containsKey (identification)) {
			return;
		}
		
		Logger.debug (String.format ("Actor for service %s has terminated", identification.toString ()));
		
		final ActorRef ref = serviceIdentifications.get (identification);
		serviceIdentifications.remove (identification);
		actorRefs.remove (ref);
		final Cancellable cancellable = serviceTerminationSchedule.remove (identification);
		
		if (cancellable != null) {
			cancellable.cancel ();
		}
	}
	
	private void removeService (final ActorRef ref) {
		if (!actorRefs.containsKey (ref)) {
			return;
		}
		
		final ServiceIdentification identification = actorRefs.get (ref);
		removeService (identification);
	}
	
	private final static class DestroyService extends ServiceMessage {

		private static final long serialVersionUID = 4750596032236303988L;

		public DestroyService (final ServiceIdentification identification) {
			super (identification);
		}
	}
}