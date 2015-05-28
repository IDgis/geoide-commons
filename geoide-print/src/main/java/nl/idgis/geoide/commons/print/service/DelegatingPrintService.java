package nl.idgis.geoide.commons.print.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.print.common.Capabilities;
import nl.idgis.geoide.commons.print.common.Capabilities.InputFormat;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.commons.print.service.PrintException.UnsupportedFormat;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.util.Futures;
import nl.idgis.ogc.util.MimeContentType;

/**
 * A print service implementation that delegates each request to one of the contained service references.
 * Each request is delegated to the service with the lowest queue length that supports the formats in
 * the request.
 * 
 * The capabilities of the delegating print service is a combination of the capabilities of all contained
 * services. When requesting the capabilities, the capabilities of each service are accessed.
 */
public class DelegatingPrintService implements PrintService {
	
	private final Set<PrintService> printServices;

	/**
	 * Constructs a delegating print service.
	 * 
	 * @param printServices References to the contained service. At least one service must be provided.
	 */
	public DelegatingPrintService (final PrintService ... printServices) {
		this (Arrays.asList (printServices));
	}

	/**
	 * Constructs a delegating print service.
	 * 
	 * @param printServices References to the contained service. At least one service must be provided.
	 */
	public DelegatingPrintService (final Collection<PrintService> printServices) {
		if (printServices == null || printServices.isEmpty ()) {
			throw new IllegalArgumentException ("At least one print service must be provided");
		}
		
		this.printServices = new HashSet<> (printServices);
	}
	
	/**
	 * Delegates the print request to the contained service with the lowest queue length that supports
	 * the requested format conversion.
	 * The resulting promise raises an exception of type {@link UnsupportedFormat} if the format
	 * conversion is not supported by any of the services.
	 * 
	 * @see PrintService#print(PrintRequest)
	 */
	@Override
	public CompletableFuture<Document> print (final PrintRequest printRequest) {
		return getServiceCapabilities ().thenCompose ((final List<ServiceCapabilities> serviceCapabilities) -> {
			// Locate all candidate services:
			ServiceCapabilities bestCapabilities = null;
			
			for (final ServiceCapabilities serviceCapability: serviceCapabilities) {
				final InputFormat inputFormat = serviceCapability.capabilities.getInputFormat (printRequest.getInputDocument ().getContentType ());
				if (inputFormat == null) {
					continue;
				}
				
				if (!inputFormat.getOutputFormats ().contains (printRequest.getOutputFormat ())) {
					continue;
				}
				
				if (bestCapabilities == null || serviceCapability.capabilities.getQueueLength () < bestCapabilities.capabilities.getQueueLength ()) {
					bestCapabilities = serviceCapability;
				}
			}
			
			if (bestCapabilities == null) {
				return Futures.<Document>throwing (new PrintException.UnsupportedFormat (printRequest.getInputDocument ().getContentType (), printRequest.getOutputFormat ()));
			}
			
			return bestCapabilities.printService.print (printRequest);
		});
	}

	/**
	 * Returns the capabilities of this service. The capabilities consist of a composition of all capabilities
	 * of linksed services. Invoking getCapabilities requests the capabilities of all underlying services.
	 * 
	 * @see PrintService#getCapabilities()
	 */
	@Override
	public CompletableFuture<Capabilities> getCapabilities () {
		return getServiceCapabilities ().thenApply ((final List<ServiceCapabilities> serviceCapabilities) -> {
			long queueLength = 0;
			final Map<MimeContentType, MutableInputFormat> mutableInputFormats = new HashMap<> ();
			
			for (final ServiceCapabilities serviceCapability: serviceCapabilities) {
				// Update the queue length:
				queueLength = Math.max (queueLength, serviceCapability.capabilities.getQueueLength ());
				
				// Update the map of supported formats:
				for (final InputFormat inputFormat: serviceCapability.capabilities.getInputFormats ()) {
					if (!mutableInputFormats.containsKey (inputFormat.getContentType ())) {
						mutableInputFormats.put (inputFormat.getContentType (), new MutableInputFormat ());
					}
					
					final MutableInputFormat mif = mutableInputFormats.get (inputFormat.getContentType ());
					
					for (final MimeContentType of: inputFormat.getOutputFormats ()) {
						mif.outputFormats.add (of);
					}
					
					for (final MimeContentType slm: inputFormat.getSupportedLinkedMedia ()) {
						mif.supportedLinkedMedia.add (slm);
					}
				}
			}
			
			final List<InputFormat> inputFormats = new ArrayList<> (mutableInputFormats.size ());
			for (final Map.Entry<MimeContentType, MutableInputFormat> entry: mutableInputFormats.entrySet ()) {
				inputFormats.add (new InputFormat (entry.getKey (), entry.getValue ().supportedLinkedMedia, entry.getValue ().outputFormats));
			}
			
			return new Capabilities (inputFormats, queueLength);
		});
	}
	
	private CompletableFuture<List<ServiceCapabilities>> getServiceCapabilities () {
		final List<PrintService> printServices = new ArrayList<> (this.printServices);
		final List<CompletableFuture<Capabilities>> capabilitiesList = new ArrayList<> (printServices.size ());
		
		for (final PrintService printService: printServices) {
			capabilitiesList.add (printService.getCapabilities ());
		}
		
		@SuppressWarnings("unchecked")
		final CompletableFuture<Capabilities>[] allFuturesArray = capabilitiesList.stream ().toArray (CompletableFuture[]::new);
		
		return CompletableFuture
			.allOf (allFuturesArray)
			.thenApply ((o) -> {
				final List<ServiceCapabilities> result = new ArrayList<> (printServices.size ());
				
				for (int i = 0; i < printServices.size (); ++ i) {
					try {
						result.add (new ServiceCapabilities (printServices.get (i), capabilitiesList.get (i).get ()));
					} catch (Exception e) {
						throw new RuntimeException (e);
					}
				}
				
				return Collections.unmodifiableList (result);
			});
	}
	
	private final static class ServiceCapabilities {
		public final PrintService printService;
		public final Capabilities capabilities;
		
		public ServiceCapabilities (final PrintService printService, final Capabilities capabilities) {
			this.printService = printService;
			this.capabilities = capabilities;
		}
	}
	
	private final static class MutableInputFormat {
		public final Set<MimeContentType> supportedLinkedMedia = new HashSet<> ();
		public final Set<MimeContentType> outputFormats = new HashSet<> ();
		
	}
}
