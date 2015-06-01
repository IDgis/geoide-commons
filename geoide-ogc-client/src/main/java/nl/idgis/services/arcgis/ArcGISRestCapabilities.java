package nl.idgis.services.arcgis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.idgis.services.Capabilities;

public class ArcGISRestCapabilities extends Capabilities {
	private static final long serialVersionUID = 1488053156053325012L;
	
	private final String version;
	private final ServiceIdentification serviceIdentification;
	private final Map<String, Layer> allLayers;
	private final List<Layer> layers;
	
	public ArcGISRestCapabilities (final String version, final ServiceIdentification serviceIdentification, final List<Layer> layers) {
		if (version == null) {
			throw new NullPointerException ("version cannot be null");
		}
		if (serviceIdentification == null) {
			throw new NullPointerException ("serviceIdentification cannot be null");
		}
		
		this.version = version;
		this.serviceIdentification = serviceIdentification;
		this.layers = layers == null || layers.isEmpty () ? Collections.<Layer>emptyList () : new ArrayList<> (layers);
		this.allLayers = new HashMap<> ();
		
		addLayers (this.layers);
	}
	
	private void addLayers (final List<Layer> layers) {
		for (final Layer layer: layers) {
			allLayers.put (layer.id (), layer);
			addLayers (layer.layers ());
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

	@Override
	public List<Layer> layers () {
		return Collections.unmodifiableList (layers);
	}

	@Override
	public Collection<Layer> allLayers () {
		return allLayers.values ();
	}

	@Override
	public boolean hasLayer (final String name) {
		return allLayers.containsKey (name);
	}

	@Override
	public Layer layer (final String name) {
		return allLayers.get (name);
	}

	public final static class ServiceIdentification implements Capabilities.ServiceIdentification {
		private static final long serialVersionUID = -358141345178233760L;
		
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
		public String abstractText () {
			return abstractText;
		}
	}
	
	public final static class Layer implements Capabilities.Layer {
		private static final long serialVersionUID = -6912961710068238981L;
		
		private final String id;
		private final String name;
		private final List<Layer> layers;
		private final String crs;

		public Layer (final String id, final String name, final String crs, final List<Layer> layers) {
			if (id == null) {
				throw new NullPointerException ("id cannot be null");
			}
			if (name == null) {
				throw new NullPointerException ("name cannot be null");
			}
			if (crs == null) {
				throw new NullPointerException ("crs cannot be null");
			}
			
			this.id = id;
			this.name = name;
			this.crs = crs;
			this.layers = layers == null || layers.isEmpty () ? Collections.<Layer>emptyList() : new ArrayList<> (layers);
		}
		
		public String id () {
			return this.id;
		}
		
		@Override
		public String name () {
			return id ();
		}

		@Override
		public String title () {
			return name;
		}

		@Override
		public Set<String> crss () {
			return new HashSet<String> (Arrays.asList (new String[] { crs }));
		}

		@Override
		public boolean supportsCRS (final String crs) {
			if (crs == null) {
				return false;
			}
			
			for (final String c: crss ()) {
				if (c.toLowerCase ().equals (crs.toLowerCase ())) {
					return true;
				}
			}
			
			return false;
		}

		@Override
		public List<Layer> layers () {
			return Collections.unmodifiableList (layers);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Layer other = (Layer) obj;
			return id.equals (other.id ());
		}
	}
}
