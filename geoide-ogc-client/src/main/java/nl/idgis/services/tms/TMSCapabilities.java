package nl.idgis.services.tms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.services.Capabilities;
import nl.idgis.services.Capabilities.BoundingBox;
import nl.idgis.services.Capabilities.Point;

public class TMSCapabilities  {

	public final static class TileMapService extends Capabilities {
		private static final long serialVersionUID = -8224609474113494633L;
		
		private final String version;
		private final ServiceIdentification serviceIdentification;
		private final List<TileMap> tileMaps;
		private final LinkedHashMap<String, TMSCapabilities.Layer> layers;
		
		public TileMapService (final String version, final String title, final String abstractText, final List<TileMap> tileMaps) {
			if (version == null) {
				throw new NullPointerException ("version cannot be null");
			}
			if (title == null) {
				throw new NullPointerException ("title cannot be null");
			}
			
			this.version = version;
			this.serviceIdentification = new ServiceIdentification (title, abstractText);
			this.tileMaps = tileMaps == null ? Collections.<TileMap>emptyList () : new ArrayList<> (tileMaps);
			
			// Construct layers:
			final LinkedHashMap<String, List<TileMap>> tileMapGroups = new LinkedHashMap<> ();
			for (final TileMap tileMap: this.tileMaps) {
				final String mapTitle = tileMap.title ();
				if (!tileMapGroups.containsKey (mapTitle)) {
					tileMapGroups.put (mapTitle, new ArrayList<TileMap> ());
				}
				tileMapGroups.get (mapTitle).add (tileMap);
			}
			
			this.layers = new LinkedHashMap<> ();
			for (final Map.Entry<String, List<TileMap>> group: tileMapGroups.entrySet ()) {
				this.layers.put (group.getKey (), new TMSCapabilities.Layer (group.getValue ()));
			}
		}
		
		@Override
		public String version () {
			return version;
		}
	
		@Override
		public ServiceIdentification serviceIdentification () {
			return serviceIdentification;
		}
	
		public List<TileMap> tileMaps () {
			return Collections.unmodifiableList (tileMaps);
		}
		
		@Override
		public Collection<TMSCapabilities.Layer> layers () {
			return layers.values ();
		}
	
		@Override
		public Collection<? extends Layer> allLayers () {
			return layers.values ();
		}
	
		@Override
		public boolean hasLayer (final String name) {
			return layers.containsKey (name);
		}
	
		@Override
		public Layer layer (final String name) {
			return layers.get (name);
		}
		
		public static class ServiceIdentification implements Capabilities.ServiceIdentification {
			private static final long serialVersionUID = 8565015826061888455L;
			
			private final String title;
			private final String abstractText;
	
			public ServiceIdentification (final String title, final String abstractText) {
				this.title = title;
				this.abstractText = abstractText;
			}
			
			@Override
			public String title () {
				return title;
			}
	
			@Override
			public String abstractText() {
				return abstractText;
			}
		}
	}

	public static class Layer implements Capabilities.Layer {

		private static final long serialVersionUID = 4311249508974361664L;
		
		private final List<TileMap> tileMaps;
		private final String title;
		private final Map<String, List<TileMap>> crsTileMaps;
		
		Layer (final List<TileMap> tileMaps) {
			if (tileMaps == null || tileMaps.isEmpty ()) {
				throw new IllegalArgumentException ("At least one tilemap must be provided");
			}
			
			this.tileMaps = new ArrayList<> (tileMaps);
			this.title = this.tileMaps.get (0).title (); 
			
			// Build the CRS to tilemap map:
			this.crsTileMaps = new HashMap<> ();
			for (final TileMap tileMap: tileMaps) {
				if (!this.title.equals (tileMap.title ())) {
					throw new IllegalArgumentException ("All tilemaps in a layer must have the same title");
				}
				
				final String crs = tileMap.srs ();
				
				if (!this.crsTileMaps.containsKey (crs)) {
					this.crsTileMaps.put (crs, new ArrayList<TileMap> ());
				}
				
				this.crsTileMaps.get (crs).add (tileMap);
			}
		}
		
		public List<TileMap> tileMaps () {
			return Collections.unmodifiableList (tileMaps);
		}
		
		@Override
		public String name () {
			return title;
		}

		@Override
		public String title () {
			return title;
		}

		@Override
		public Set<String> crss () {
			return crsTileMaps.keySet ();
		}

		@Override
		public boolean supportsCRS (final String crs) {
			if (crs == null) {
				return false;
			}
			
			for (final TileMap tileMap: tileMaps) {
				if (tileMap.srs ().toLowerCase ().equals (crs.toLowerCase ())) {
					return true;
				}
			}
			
			return false;
		}

		@Override
		public Collection<? extends Capabilities.Layer> layers () {
			return Collections.<Capabilities.Layer>emptyList ();
		}
	}
	
	public static class TileMap implements Serializable {
		private static final long serialVersionUID = 647411988225229535L;
		
		private final String title;
		private final String srs;
		private final String profile;
		private final String href;
		
		public TileMap (final String title, final String srs, final String profile, final String href) {
			if (title == null) {
				throw new NullPointerException ("title cannot be null");
			}
			if (srs == null) {
				throw new NullPointerException ("srs cannot be null");
			}
			if (profile == null) {
				throw new NullPointerException ("profile cannot be null");
			}
			if (href == null) {
				throw new NullPointerException ("href cannot be null");
			}
			
			this.title = title;
			this.srs = srs;
			this.profile = profile;
			this.href = href;
		}
		
		protected TileMap (final TileMap original) {
			this.title = original.title ();
			this.srs = original.srs ();
			this.profile = original.profile ();
			this.href = original.href ();
		}
		
		public String title () {
			return this.title;
		}
		
		public String srs () {
			return this.srs;
		}
		
		public String profile () {
			return this.profile;
		}
		
		public String href () {
			return this.href;
		}
	}
	
	public final static class TileMapLayer extends TileMap {
		private static final long serialVersionUID = 5055409890525166493L;
		
		private final String version;
		private final String abstractText;
		private final BoundingBox boundingBox;
		private final Point origin;
		private final TileFormat tileFormat;
		private final List<TileSet> tileSets;
		
		public TileMapLayer (final TileMap original, final String version, final String abstractText, final BoundingBox boundingBox, final Point origin, final TileFormat tileFormat, final List<TileSet> tileSets) {
			super (original);
			
			if (version == null) {
				throw new NullPointerException ("version cannot be null");
			}
			if (boundingBox == null) {
				throw new NullPointerException ("boundingBox cannot be null");
			}
			if (origin == null) {
				throw new NullPointerException ("origin cannot be null");
			}
			if (tileFormat == null) {
				throw new NullPointerException ("tileFormat cannot be null");
			}
			if (tileSets == null || tileSets.isEmpty ()) {
				throw new IllegalArgumentException ("At least one tileset must be provided");
			}
			
			this.version = version;
			this.abstractText = abstractText;
			this.boundingBox = boundingBox;
			this.origin = origin;
			this.tileFormat = tileFormat;
			this.tileSets = new ArrayList<> (tileSets);
		}
		
		public String version () {
			return version;
		}
		
		public String abstractText () {
			return abstractText;
		}
		
		public BoundingBox boundingBox () {
			return boundingBox;
		}
		
		public Point origin () {
			return origin;
		}
		
		public TileFormat tileFormat () {
			return tileFormat;
		}
		
		public List<TileSet> tileSets () {
			return Collections.unmodifiableList (tileSets);
		}
	}
	
	public final static class TileFormat implements Serializable {
		private static final long serialVersionUID = -5148281935487853952L;
		
		private final int width;
		private final int height;
		private final MimeContentType mimeType;
		private final String extension;
		
		public TileFormat (final int width, final int height, final MimeContentType mimeType, final String extension) {
			if (mimeType == null) {
				throw new NullPointerException ("mimeType cannot be null");
			}
			if (extension == null) {
				throw new NullPointerException ("extension cannot be null");
			}
			
			this.width = width;
			this.height = height;
			this.mimeType = mimeType;
			this.extension = extension;
		}
		
		public int width () {
			return width;
		}
		
		public int height () {
			return height;
		}
		
		public MimeContentType mimeType () {
			return mimeType;
		}
		
		public String extension () {
			return extension;
		}
	}
	
	public final static class TileSet implements Serializable {
		private static final long serialVersionUID = 6357845154322129606L;
		
		private final String href;
		private final double unitsPerPixel;
		private final int order;
		
		public TileSet (final String href, final double unitsPerPixel, final int order) {
			this.href = href;
			this.unitsPerPixel = unitsPerPixel;
			this.order = order;
		}
		
		public String href () {
			return href;
		}
		
		public double unitsPerPixel () {
			return unitsPerPixel;
		}
		
		public int order () {
			return order;
		}
	}
}
