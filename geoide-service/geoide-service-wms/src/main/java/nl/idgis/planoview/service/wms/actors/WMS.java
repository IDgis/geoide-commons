package nl.idgis.planoview.service.wms.actors;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import nl.idgis.ogc.client.wms.WMSCapabilitiesParser;
import nl.idgis.ogc.client.wms.WMSCapabilitiesParser.ParseException;
import nl.idgis.ogc.util.MimeContentType;
import nl.idgis.ogc.wms.WMSCapabilities;
import nl.idgis.planoview.commons.domain.ServiceIdentification;
import nl.idgis.planoview.service.actors.OGCService;
import nl.idgis.planoview.service.messages.OGCServiceRequest;
import nl.idgis.planoview.service.messages.OGCServiceResponse;
import nl.idgis.planoview.service.messages.ServiceCapabilities;
import nl.idgis.planoview.service.messages.ServiceError;
import nl.idgis.planoview.service.messages.ServiceErrorType;
import nl.idgis.planoview.service.messages.ServiceMessage;
import nl.idgis.planoview.service.messages.ServiceMessageContext;
import nl.idgis.planoview.service.messages.ServiceRequest;
import nl.idgis.services.Capabilities;
import nl.idgis.services.OGCCapabilities;
import play.Logger;
import play.libs.F.Promise;
import play.libs.ws.WSResponse;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.util.ByteString;

public class WMS extends OGCService {
	
	public WMS (final ActorRef serviceManager, final ServiceIdentification identification) {
		super(serviceManager, identification, DEFAULT_CACHE_LIFETIME, DEFAULT_CAPABILITIES_TIMEOUT, DEFAULT_REQUEST_TIMEOUT);
	}
	
	public static Props mkProps (final ActorRef serviceManager, final ServiceIdentification identification) {
		return Props.create (WMS.class, serviceManager, identification);
	}

	@Override
	protected boolean handleServiceMessage (final ServiceMessage message) throws Throwable {
		if (super.handleServiceMessage (message)) {
			return true;
		} else if (message instanceof GetLegendGraphic) {
			handleGetLegendGraphic ((GetLegendGraphic) message);
			return true;
		} else if (message.context () != null && message.context ().originalMessage () != null && message.context ().originalMessage () instanceof GetLegendGraphic) {
			handleGetLegendGraphicResponse ((GetLegendGraphic)message.context ().originalMessage (), message.context ().sender (), message);
			return true;
		}
		
		return false;
	}

	private void handleGetLegendGraphic (final GetLegendGraphic getLegendGraphic) {
		final Map<String, String[]> parameters = new HashMap<> ();
		
		parameters.put ("LAYER", new String[] { getLegendGraphic.layer });
		parameters.put ("FORMAT", new String[] { "image/png" });
		
		final OGCServiceRequest request = new OGCServiceRequest (getLegendGraphic.serviceIdentification (), "GetLegendGraphic", parameters, new ServiceMessageContext (sender (), getLegendGraphic));
		
		// Send the OGC service request to self:
		self ().tell (request, self ());
	}
	
	private void handleGetLegendGraphicResponse (final GetLegendGraphic getLegendGraphic, final ActorRef sender, final ServiceMessage response) {
		if (response instanceof ServiceError) {
			// Delegate the service error:
			sender.tell (response, self ());
		} else if (response instanceof OGCServiceResponse) {
			parseResponse (getLegendGraphic, sender, (OGCServiceResponse)response);
		}
	}
	
	private void parseResponse (final GetLegendGraphic getLegendGraphic, final ActorRef sender, final OGCServiceResponse response) {
		// Test the content type:
		final MimeContentType contentType = new MimeContentType (response.contentType ());
		
		if (!contentType.matches ("image/png") && !contentType.matches ("image/jpeg") && !contentType.matches ("image/jpg") && !contentType.matches ("image/gif")) {
			sender.tell (raiseServiceError (sender, self (), ServiceErrorType.FORMAT_ERROR, response.url (), String.format ("Invalid content type: %s", contentType), null, getLegendGraphic.context ()), self ());
			return;
		}
		
		// Attempt to load the image:
		final BufferedImage image;
		try {
			image = ImageIO.read (response.data ().iterator ().asInputStream ());
		} catch (IOException e) {
			sender.tell (raiseServiceError (sender, self (), ServiceErrorType.FORMAT_ERROR, response.url (), e.getMessage (), e, getLegendGraphic.context ()), self ());
			return;
		}
		
		// Serialize the image as a PNG:
		final ByteArrayOutputStream os = new ByteArrayOutputStream ();
		try {
			ImageIO.write (image, "PNG", os);
		} catch (IOException e) {
			sender.tell (raiseServiceError (sender, self (), ServiceErrorType.EXCEPTION, response.url (), e.getMessage (), e, getLegendGraphic.context ()), self ());
			return;
		}
		
		// Create a GetLegendGraphic response:
		final OGCCapabilities capabilities = response.capabilities ();
		final OGCCapabilities.Layer layer = capabilities.layer (getLegendGraphic.layer ());
		final GetLegendGraphicResponse responseMessage = new GetLegendGraphicResponse (
			identification (), 
			getLegendGraphic.layer (), 
			image.getWidth (), 
			image.getHeight (), 
			ByteString.fromArray (os.toByteArray ()),
			layer == null || layer.title () == null ? getLegendGraphic.layer () : layer.title ()
		);
		
		image.flush ();

		sender.tell (responseMessage, self ());
	}
	
	@Override
	protected String service () {
		return "WMS";
	}

	@Override
	protected ServiceMessage parseCapabilities (final WSResponse response, final String url, final ActorRef sender, final ActorRef self, final ServiceMessageContext context) {
		Logger.debug (String.format ("Parsing capabilities document for service %s", identification ().toString ()));
		
		final WMSCapabilities capabilities;
		
		try {
			capabilities = WMSCapabilitiesParser.parseCapabilities (response.getBodyAsStream ());
		} catch (ParseException e) {
			return raiseServiceError (sender, self, ServiceErrorType.FORMAT_ERROR, url, e.getMessage (), e, context);
		}
		
		return new ServiceCapabilities (identification (), capabilities);
	}
	
	public final static class GetLegendGraphic extends ServiceMessage {
		private static final long serialVersionUID = 2255933087751183857L;
		
		private final String layer;
		
		public GetLegendGraphic (final ServiceIdentification identification, final String layer) {
			super (identification);
			
			if (layer == null) {
				throw new NullPointerException ("layer cannot be null");
			}
			
			this.layer = layer;
		}
		
		public String layer () {
			return layer;
		}
	}
	
	public final static class GetLegendGraphicResponse extends ServiceMessage {
		private static final long serialVersionUID = 8157564189789283142L;
		
		private final String layer;
		private final int width;
		private final int height;
		private final ByteString imageData;
		private final String title;
		
		public GetLegendGraphicResponse (final ServiceIdentification identification, final String layer, final int width, final int height, final ByteString imageData, final String title) {
			super (identification);
			
			if (layer == null) {
				throw new NullPointerException ("layer cannot be null");
			}
			if (imageData == null) {
				throw new NullPointerException ("imageData cannot be null");
			}
			if (title == null) {
				throw new NullPointerException ("title cannot be null");
			}

			this.layer = layer;
			this.width = width;
			this.height = height;
			this.imageData = imageData;
			this.title = title;
		}
		
		public String layer () {
			return layer;
		}
		
		public int width () {
			return width;
		}
		
		public int height () {
			return height;
		}
		
		public ByteString imageData () {
			return imageData;
		}
		
		public String title () {
			return title;
		}
	}

	@Override
	protected Promise<ServiceMessage> handleServiceRequest(
			ServiceRequest request, Capabilities capabilities, ActorRef sender,
			ActorRef self) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
}
