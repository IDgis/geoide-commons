package nl.idgis.geoide.commons.config;

import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.MapQuery;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.commons.domain.api.PrintService;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.domain.api.ServiceProviderApi;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.commons.domain.api.TemplateDocumentProvider;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;
import nl.idgis.geoide.commons.remote.ServiceRegistration;
import nl.idgis.geoide.commons.remote.transport.AkkaTransport;
import nl.idgis.geoide.util.ConfigWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorSystem;

@Configuration
public class RemoteApiConfig {
	private final static Logger log = LoggerFactory.getLogger (RemoteApiConfig.class);
	
	@Bean
	public RemoteServiceFactory remoteServiceFactory () {
		return new RemoteServiceFactory ();
	}
	
	/**
	 * Creates a remote transport actor.
	 * 
	 * @param actorSystem		The Akka actorsystem to use.
	 * @param config			Application configuration object.
	 * @return					A new AkkaTransport.
	 */
	@Bean
	@Autowired
	public AkkaTransport akkaTransport (final ActorSystem actorSystem, final ConfigWrapper config) {
		final String actorName = config.getString ("geoide.service.components.remoteMethodServer.actorName", "remote-method-server");
		final long timeout = config.getLong ("geoide.service.components.remoteMethodServer.timeoutInMillis", 10000);
		
		log.info ("Creating akka remote transport actor: " + actorName + " (timeout: " + timeout + " ms)");
		
		return new AkkaTransport (
				actorSystem, 
				actorName,
				timeout
			);
	}
	
	/**
	 * Creates a remote method server by registering all API components.
	 * 
	 * @param factory	The remote service factory instance to use when creating the server.
	 * @param transport	The Akka transport to use.
	 * @param config	The application config object.
	 * @param mapView	The MapView component to use.
	 * @return 			A remote method server containing the given components.
	 */
	@Bean
	@Autowired
	public RemoteMethodServer remoteMethodServer (
			final RemoteServiceFactory factory,
			final AkkaTransport transport,
			final ConfigWrapper config,
			final MapView mapView,
			final @Qualifier ("printDocumentCache") DocumentCache printDocumentCache,
			final PrintService printService,
			final ReportComposer reportComposer,
			final TemplateDocumentProvider templateDocumentProvider,
			final MapQuery mapQuery,
			final TableOfContents toc,
			final ServiceProviderApi serviceProviderApi) {
		
		final String serverName = config.getString ("geoide.service.components.remoteMethodServer.apiServerName", "api");
		
		log.info ("Creating server for remote API access: " + serverName);
		
		final RemoteMethodServer server = factory.createRemoteMethodServer (
				new ServiceRegistration<MapView> (MapView.class, mapView, null),
				new ServiceRegistration<DocumentCache> (DocumentCache.class, printDocumentCache, config.getString ("geoide.service.components.print.cacheName", "geoide-print")),
				new ServiceRegistration<PrintService> (PrintService.class, printService, null),
				new ServiceRegistration<ReportComposer> (ReportComposer.class, reportComposer, null),
				new ServiceRegistration<TemplateDocumentProvider> (TemplateDocumentProvider.class, templateDocumentProvider, null),
				new ServiceRegistration<MapQuery> (MapQuery.class, mapQuery, null),
				new ServiceRegistration<TableOfContents> (TableOfContents.class, toc, null),
				new ServiceRegistration<ServiceProviderApi> (ServiceProviderApi.class, serviceProviderApi, null)
			);
		
		transport.listen (server, serverName);
		
		return server;
	}
}
