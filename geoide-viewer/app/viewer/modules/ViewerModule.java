package viewer.modules;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.MapProviderApi;
import nl.idgis.geoide.commons.domain.api.MapQuery;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.domain.api.ServiceProviderApi;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.commons.domain.api.TemplateDocumentProvider;
import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;
import nl.idgis.geoide.commons.remote.transport.AkkaTransport;
import nl.idgis.geoide.util.AkkaFutures;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.Configuration;
import play.Play;
import play.libs.Akka;
import play.libs.akka.AkkaGuiceSupport;
import scala.concurrent.duration.FiniteDuration;

public class ViewerModule extends AbstractModule implements AkkaGuiceSupport {

	@Override
	protected void configure() {
	}
	
	@Provides
	@Singleton
	public MapView mapView (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, MapView.class);
	}

	@Provides
	@Singleton
	public MapQuery mapQuery (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, MapQuery.class);
	}

	@Provides
	@Singleton
	public TableOfContents tableOfContents (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, TableOfContents.class);
	}

	@Provides
	@Singleton
	public ServiceProviderApi serviceProviderApi (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, ServiceProviderApi.class);
	}

	@Provides
	@Singleton
	public MapProviderApi mapProviderApi (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, MapProviderApi.class);
	}
	
	@Provides
	//@Named ("printDocumentCache")
	@Singleton
	public DocumentCache printDocumentCache (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, DocumentCache.class, Play.application ().configuration().getString ("geoide.web.print.documentCacheQualifier"));
	}

	@Provides
	@Singleton
	public PrintService htmlPrintService (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, PrintService.class);
	}

	@Provides
	@Singleton
	public ReportComposer reportComposer (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, ReportComposer.class);
	}

	@Provides
	@Singleton
	public TemplateDocumentProvider templateDocumentProvider (final RemoteServiceFactory factory, final RemoteMethodClient client) {
		return factory.createServiceReference (client, TemplateDocumentProvider.class);
	}

	@Provides
	@Singleton
	public RemoteServiceFactory remoteServiceFactory () {
		return new RemoteServiceFactory ();
	}
	
	@Provides
	@Singleton
	public AkkaTransport akkaTransport (final ActorSystem actorSystem, final Configuration configuration) {
		final String actorName = configuration.getString ("geoide.web.remoteMethodClient.actorName", "remote-method-client");
		final long timeout = configuration.getLong ("geoide.web.remoteMethodClient.timeoutInMillis", 10000l);
		
		return new AkkaTransport (actorSystem, actorName, timeout);
	}

	@Provides
	@Singleton
	public RemoteMethodClient remoteMethodClient (final AkkaTransport transport, final Configuration configuration) {
		final String remoteActorRef = configuration.getString ("geoide.web.actors.remoteMethodServer", "akka.tcp://service@127.0.0.1:2552/user/remote-method-server");
		final String apiServerName = configuration.getString ("geoide.web.remoteMethodClient.apiServerName", "api");
		
		return transport.connect (remoteActorRef, apiServerName);
	}
	
	@Provides
	@Singleton
	public ActorRef serviceManagerActor (final ActorSystem actorSystem, final Configuration configuration) throws InterruptedException, ExecutionException, TimeoutException {
		final ActorSelection selection = actorSystem.actorSelection (configuration.getString ("geoide.web.actors.serviceManager"));
		final ActorRef actorRef = AkkaFutures.asCompletableFuture (selection.resolveOne (new FiniteDuration (10000, TimeUnit.MILLISECONDS)), Akka.system ().dispatcher ()).get (10000, TimeUnit.MILLISECONDS);
		
		return actorRef;
	}
	
	@Provides
	@Singleton
	public StreamProcessor streamProcessor (final ActorSystem actorSystem) {
		return new AkkaStreamProcessor (actorSystem);
	}
	
}
