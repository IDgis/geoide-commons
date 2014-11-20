package nl.idgis.geoide.service.actors;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import nl.idgis.geoide.service.messages.CacheMiss;
import nl.idgis.geoide.service.messages.CacheObject;
import nl.idgis.geoide.service.messages.CacheResponse;
import nl.idgis.geoide.service.messages.CachedObject;
import nl.idgis.geoide.service.messages.RetrieveCachedObject;

import org.joda.time.LocalDateTime;

import akka.actor.Props;
import akka.actor.UntypedActor;

public class ObjectCache extends UntypedActor {

	private final PriorityQueue<Entry> entries = new PriorityQueue<> (new Comparator<Entry> () {
		@Override
		public int compare (final Entry o1, final Entry o2) {
			return o1.getExpiryTime ().compareTo (o2.getExpiryTime ());
		}
	});
	private final Map<String, Entry> entryMap = new HashMap<> ();
	
	private ObjectCache () {
	}
	
	public static Props props () {
		return Props.create (ObjectCache.class);
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof CacheObject) {
			final Entry entry = entry (((CacheObject) message).getObject (), ((CacheObject) message).getTtl ());
			
			entries.add (entry);
			entryMap.put (entry.getKey (), entry);
			
			sender ().tell (new CacheResponse (entry.getKey (), entry.getExpiryTime ()), self ());
		} else if (message instanceof RetrieveCachedObject) {
			final String key = ((RetrieveCachedObject) message).getKey ();
			final Entry entry = entryMap.get (key);
			
			if (entry == null) {
				sender ().tell (new CacheMiss (key), self ());
			} else {
				sender ().tell (new CachedObject (entry.getKey (), entry.getExpiryTime (), entry.getValue ()), self ());
			}
		} else {
			unhandled (message);
		}
	}
	
	private Entry entry (final Serializable value, final int ttlMillis) {
		return new Entry (UUID.randomUUID ().toString (), LocalDateTime.now (), ttlMillis, value);
	}
	
	public final class Entry {
		private final String key; 
		private final LocalDateTime time;
		private final int ttlMillis;
		private final Serializable value;
		
		public Entry (final String key, final LocalDateTime time, final int ttlMillis, final Serializable value) {
			if (key == null) {
				throw new NullPointerException ("key cannot be null");
			}
			if (time == null) {
				throw new NullPointerException ("time cannot be null");
			}
			if (value == null) {
				throw new NullPointerException ("value cannot be null");
			}

			this.key = key;
			this.time = time;
			this.ttlMillis = ttlMillis;
			this.value = value;
		}

		public String getKey () {
			return key;
		}
		
		public LocalDateTime getTime () {
			return time;
		}

		public int getTtlMillis () {
			return ttlMillis;
		}

		public Serializable getValue () {
			return value;
		}
		
		public LocalDateTime getExpiryTime () {
			return time.plusMillis (ttlMillis);
		}
	}
}
