package nl.idgis.geoide.service.actors;

import java.util.concurrent.atomic.AtomicReference;

import play.libs.F.Function0;

public class CachedReference<T> {
	
	private AtomicReference<Container<T>> value = new AtomicReference<> ();
	
	public T get (final Function0<T> factory, final long retention) throws Throwable {
		final long currentTime = System.currentTimeMillis ();
		final Container<T> currentValue = value.get ();
		
		if (currentValue == null || currentTime >= currentValue.invalidateTime) {
			synchronized (value) {
				final Container<T> lockedCurrentValue = value.get ();
				if (lockedCurrentValue == null || currentTime >= lockedCurrentValue.invalidateTime) {
					value.set (new Container<T> (factory.apply (), currentTime + retention));
					return value.get ().value;
				}
				
				return lockedCurrentValue.value;
			}
		}
		
		return currentValue.value;
	}
	
	public void reset () {
		value.set (null);
	}
	
	private static class Container<T> {
		public final T value;
		public final long invalidateTime;
		
		public Container (final T value, final long invalidateTime) {
			this.value = value;
			this.invalidateTime = invalidateTime;
		}
	}
}
