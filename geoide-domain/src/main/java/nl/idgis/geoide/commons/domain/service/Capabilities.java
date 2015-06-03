package nl.idgis.geoide.commons.domain.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public abstract class Capabilities implements Serializable {
	
	private static final long serialVersionUID = 5299899942327681892L;

	public abstract String version ();
	public abstract ServiceIdentification serviceIdentification ();
	
	
	public abstract Collection<? extends Layer> layers ();
	public abstract Collection<? extends Layer> allLayers ();
	public abstract boolean hasLayer (String name);
	public abstract Layer layer (String name);
	
	public static interface ServiceIdentification extends Serializable {
		String title ();
		String abstractText ();
	}
	
	public static interface Layer extends Serializable {
		String name ();
		String title ();
		Set<String> crss ();
		boolean supportsCRS (String crs);
		Collection<? extends Layer> layers ();
	}
	
	public final static class BoundingBox implements Serializable {
		private static final long serialVersionUID = -3452072713895632354L;
		
		private final double minX;
		private final double minY;
		private final double maxX;
		private final double maxY;
		
		public BoundingBox (final double minX, final double minY, final double maxX, final double maxY) {
			this.minX = minX;
			this.minY = minY;
			this.maxX = maxX;
			this.maxY = maxY;
		}
		
		public double minX () {
			return this.minX;
		}
		
		public double minY () {
			return this.minY;
		}
		
		public double maxX () {
			return this.maxX;
		}
		
		public double maxY () {
			return this.maxY;
		}
	}
	
	public final static class Point implements Serializable {
		private static final long serialVersionUID = -8657044314550039873L;
		
		private final double x;
		private final double y;
		
		public Point (final double x, final double y) {
			this.x = x;
			this.y = y;
		}
		
		public double x () {
			return x;
		}
		
		public double y () {
			return y;
		}
	}
}
