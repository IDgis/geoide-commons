package nl.idgis.geoide.service.messages;

import java.io.Serializable;

public interface ProducerMessage {

	public final static class Request implements Serializable {
		private static final long serialVersionUID = 7981090917920271553L;
		
		private final int n;
		
		public Request (final int n) {
			if (n <= 0) {
				throw new IllegalArgumentException ("n must be > 0");
			}
			
			this.n = n;
		}
		
		public int getN () {
			return n;
		}
	}
	
	public final static class EndOfStream implements Serializable {
		private static final long serialVersionUID = 5697485404225136262L;
	}
	
	public final static class NextItem implements Serializable {
		private static final long serialVersionUID = -662338422792176889L;
		
		private final Serializable item;
		
		public NextItem (final Serializable item) {
			this.item = item;
		}
		
		public Serializable getItem () {
			return item;
		}
	}
}
