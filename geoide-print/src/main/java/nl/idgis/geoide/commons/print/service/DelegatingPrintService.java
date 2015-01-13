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

import nl.idgis.geoide.commons.print.common.Capabilities;
import nl.idgis.geoide.commons.print.common.Document;
import nl.idgis.geoide.commons.print.common.PrintRequest;
import nl.idgis.geoide.commons.print.common.Capabilities.InputFormat;
import nl.idgis.geoide.commons.print.service.PrintException.UnsupportedFormat;
import nl.idgis.ogc.util.MimeContentType;
import play.libs.F.Function;
import play.libs.F.Promise;

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
	public Promise<Document> print (final PrintRequest printRequest) {
		return getServiceCapabilities ().flatMap (new Function<List<ServiceCapabilities>, Promise<Document>> () {
			@Override
			public Promise<Document> apply (final List<ServiceCapabilities> serviceCapabilities) throws Throwable {
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
					return Promise.throwing (new PrintException.UnsupportedFormat (printRequest.getInputDocument ().getContentType (), printRequest.getOutputFormat ()));
				}
				
				return bestCapabilities.printService.print (printRequest);
			}
		});
	}

	/**
	 * Returns the capabilities of this service. The capabilities consist of a composition of all capabilities
	 * of linksed services. Invoking getCapabilities requests the capabilities of all underlying services.
	 * 
	 * @see PrintService#getCapabilities()
	 */
	@Override
	public Promise<Capabilities> getCapabilities () {
		return getServiceCapabilities ().map (new Function<List<ServiceCapabilities>, Capabilities> () {
			@Override
			public Capabilities apply (final List<ServiceCapabilities> serviceCapabilities) throws Throwable {
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
			}
		});
	}
	
	private Promise<List<ServiceCapabilities>> getServiceCapabilities () {
		final List<PrintService> printServices = new ArrayList<> (this.printServices);
		final List<Promise<Capabilities>> capabilitiesList = new ArrayList<> (printServices.size ());
		
		for (final PrintService printService: printServices) {
			capabilitiesList.add (printService.getCapabilities ());
		}
		
		return Promise.sequence (capabilitiesList).map (new Function<List<Capabilities>, List<ServiceCapabilities>> () {
			@Override
			public List<ServiceCapabilities> apply (final List<Capabilities> capabilities) throws Throwable {
				final List<ServiceCapabilities> result = new ArrayList<> (printServices.size ());
				
				for (int i = 0; i < printServices.size (); ++ i) {
					result.add (new ServiceCapabilities (printServices.get (i), capabilities.get (i)));
				}
				
				return Collections.unmodifiableList (result);
			}
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
