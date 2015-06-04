package nl.idgis.ogc.wms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.service.Capabilities;
import nl.idgis.services.OGCCapabilities;

public final class WMSCapabilities extends OGCCapabilities {
	private static final long serialVersionUID = 9092035182400986602L;
	
	private final String version;
	private final Service service;
	private final Map<RequestType, Request> requests;
	private final Set<String> exceptionFormats;
	private final List<Layer> layers;
	private final Map<String, Layer> layersMap;
	private final List<Layer> allLayers;
	private final Map<String, Layer> allLayersMap;
	
	public WMSCapabilities (final String version, final Service service, final Collection<Request> requests, final Set<String> exceptionFormats, final Collection<Layer> layers) {
		if (version == null) {
			throw new NullPointerException ("version cannot be null");
		}
		if (service == null) {
			throw new NullPointerException ("service cannot be null");
		}

		this.version = version;
		this.service = service;
		
		this.requests = new HashMap<> ();
		if (requests != null) {
			for (final Request request: requests) {
				this.requests.put (request.type (), request);
			}
		}
		
		this.exceptionFormats = exceptionFormats == null ? Collections.<String>emptySet () : new HashSet<> (exceptionFormats);
		
		this.layers = new ArrayList<> ();
		this.layersMap = new LinkedHashMap<> ();
		if (layers != null) {
			for (final Layer layer: layers) {
				this.layers.add (layer);
				if (layer.name () != null) {
					this.layersMap.put (layer.name (), layer);
				}
			}
		}
		
		this.allLayers = new ArrayList<> ();
		this.allLayersMap = new HashMap<> ();
		final LinkedList<Layer> fringe = new LinkedList<> (this.layers);
		while (!fringe.isEmpty ()) {
			final Layer layer = fringe.poll ();
			this.allLayers.add (layer);
			if (layer.name () != null) {
				this.allLayersMap.put (layer.name (), layer);
			}
			fringe.addAll (layer.layers ());
		}
	}
	
	@Override
	public String version () {
		return this.version;
	}
	
	/**
	 * @deprecated Use serviceIdentification instead.
	 */
	@Deprecated
	public Service service () {
		return this.service;
	}
	
	@Override
	public Service serviceIdentification () {
		return this.service;
	}
	
	/**
	 * @deprecated Use hasOperation instead.
	 */
	@Deprecated
	public boolean hasRequestType (final RequestType type) {
		return this.requests.containsKey (type);
	}
	
	@Override
	public boolean hasOperation (final OGCCapabilities.OperationType type) {
		if (!(type instanceof RequestType)) {
			return false;
		}
		
		return this.requests.containsKey ((RequestType)type);
	}

	/**
	 * @deprecated Use operation instead.
	 */
	@Deprecated
	public Request request (final RequestType type) {
		return operation (type);
	}
	
	@Override
	public Request operation (final OGCCapabilities.OperationType type) {
		if (!(type instanceof RequestType)) {
			return null;
		}
		
		return this.requests.get ((RequestType)type);
	}
	
	@Override
	public Request operationByName (final String operationName) {
		final RequestType rt = RequestType.byName (operationName);
		if (rt == null) {
			return null;
		}
		
		return operation (rt);
	}

	/**
	 * @deprecated Use operations instead.
	 */
	@Deprecated
	public Collection<Request> requests () {
		return operations ();
	}
	
	@Override
	public Collection<Request> operations () {
		return Collections.unmodifiableCollection (requests.values ());
	}
	
	public Set<String> exceptionFormats () {
		return Collections.unmodifiableSet (this.exceptionFormats);
	}
	
	@Override
	public Collection<Layer> layers () {
		return Collections.unmodifiableCollection (this.layers);
	}
	
	@Override
	public Collection<Layer> allLayers () {
		return Collections.unmodifiableCollection (this.allLayers);
	}
	
	@Override
	public boolean hasLayer (final String layerName) {
		return this.allLayersMap.containsKey (layerName);
	}
	
	@Override
	public Layer layer (final String layerName) {
		return this.allLayersMap.get (layerName);
	}
	
	public final static class Service implements Capabilities.ServiceIdentification {
		private static final long serialVersionUID = 9052168883719299098L;
		
		private final String name;
		private final String title;
		private final String abstractText;
		private final Set<String> keywords;
		
		public Service (final String name, final String title, final String abstractText, final Set<String> keywords) {
			this.name = name;
			this.title = title;
			this.abstractText = abstractText;
			this.keywords = keywords == null ? Collections.<String>emptySet () : new HashSet<> (keywords);
		}
		
		public String name () {
			return this.name;
		}
		
		@Override
		public String title () {
			return this.title;
		}
		
		@Override
		public String abstractText () {
			return this.abstractText;
		}
		
		public Set<String> keywords () {
			return Collections.unmodifiableSet (keywords);
		}
	}

	public static enum RequestType implements OGCCapabilities.OperationType {
		GET_CAPABILITIES("GetCapabilities"),
		GET_MAP("GetMap"),
		GET_FEATURE_INFO("GetFeatureInfo"),
		DESCRIBE_LAYER("DescribeLayer"),
		GET_LEGEND_GRAPHIC("GetLegendGraphic");
		
		private final String name;
		
		RequestType (final String name) {
			this.name = name;
		}
		
		@Override
		public String operationName () {
			return this.name;
		}
		
		public static RequestType byName (final String name) {
			if (name == null) {
				return null;
			}
			
			final String lcName = name.toLowerCase ();
			
			for (final RequestType rt: RequestType.values ()) {
				if (rt.operationName ().toLowerCase ().equals (lcName)) {
					return rt;
				}
			}
			
			return null;
		}
	}
	
	public final static class Request implements OGCCapabilities.Operation {
		private static final long serialVersionUID = 316552820368419580L;
		
		private final RequestType type;
		private final Set<MimeContentType> formats;
		private final String httpGet;
		private final String httpPost;
		
		public Request (final RequestType type, final Set<MimeContentType> formats, final String httpGet, final String httpPost) {
			if (type == null) {
				throw new NullPointerException ("type cannot be null");
			}
			
			this.type = type;
			this.formats = formats == null ? Collections.<MimeContentType>emptySet () : new HashSet<> (formats);
			this.httpGet = httpGet;
			this.httpPost = httpPost;
		}
		
		public RequestType type () {
			return this.type;
		}
		
		@Override
		public RequestType operationType () {
			return type ();
		}
		
		public Set<MimeContentType> formats () {
			return Collections.unmodifiableSet (formats);
		}
		
		@Override
		public String httpGet () {
			return this.httpGet;
		}
		
		@Override
		public String httpPost () {
			return this.httpPost;
		}
	}
	
	public final static class Layer implements Capabilities.Layer {
		private static final long serialVersionUID = -6033395379414003132L;
		
		private final boolean queryable;
		private final String name;
		private final String title;
		private final String abstractText;
		private final Set<String> crss;
		private final Map<String, BoundingBox> boundingBoxes;
		private final Map<String, Style> styles;
		private final List<Layer> layers;
		private final Map<String, Layer> layersMap;
		
		public Layer (final boolean queryable, final String name, final String title, final String abstractText, final Set<String> crss, final Map<String, BoundingBox> boundingBoxes, final Collection<Style> styles, final Collection<Layer> layers) {
			this.queryable = queryable;
			this.name = name;
			this.title = title;
			this.abstractText = abstractText;
			this.crss = crss == null ? Collections.<String>emptySet () : new HashSet<> (crss);
			this.boundingBoxes = boundingBoxes == null ? Collections.<String, BoundingBox>emptyMap () : new HashMap<> (boundingBoxes);
			this.layers = new ArrayList<> ();
			this.layersMap = new HashMap<> ();
			
			if (layers != null) {
				for (final Layer layer: layers) {
					this.layers.add (layer);
					if (layer.name () != null) {
						this.layersMap.put (layer.name (), layer);
					}
				}
			}
			
			this.styles = new LinkedHashMap<> ();
			
			if (styles != null) {
				for (final Style style: styles) {
					this.styles.put (style.name (), style);
				}
			}
		}
		
		public boolean queryable () {
			return this.queryable;
		}
		
		@Override
		public String name () {
			return this.name;
		}
		
		@Override
		public String title () {
			return this.title;
		}
		
		public String abstractText () {
			return this.abstractText;
		}
		
		@Override
		public Set<String> crss () {
			return Collections.unmodifiableSet (this.crss);
		}
		
		@Override
		public boolean supportsCRS (final String crs) {
			if (crs == null || crs.isEmpty ()) {
				return false;
			}
			
			for (final String otherCrs: crss) {
				if (otherCrs.toLowerCase ().equals (crs.toLowerCase ())) {
					return true;
				}
			}
			
			return false;
		}
		
		public Map<String, BoundingBox> boundingBoxes () {
			return Collections.unmodifiableMap (this.boundingBoxes);
		}
		
		public boolean hasBoundingBox (final String crs) {
			return boundingBoxes.containsKey (crs);
		}
		
		public BoundingBox boundingBox (final String crs) {
			return boundingBoxes.get (crs); 
		}
		
		@Override
		public List<Layer> layers () {
			return Collections.unmodifiableList (layers);
		}
		
		public Collection<Style> styles () {
			return styles.values ();
		}
		
		public boolean hasStyle (final String name) {
			return styles.containsKey (name);
		}
		
		public Style style (final String name) {
			return styles.get (name);
		}
	}
	
	public final static class Style implements Serializable {
		private static final long serialVersionUID = -3616429767506070341L;
		
		private final String name;
		private final String title;
		
		public Style (final String name, final String title) {
			this.name = name;
			this.title = title;
		}
		
		public String name () {
			return this.name;
		}
		
		public String title () {
			return this.title;
		}
	}
}
