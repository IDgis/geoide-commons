package nl.idgis.geoide.commons.domain.traits;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.util.Assert;

public final class Traits<T> implements Serializable {
	private static final long serialVersionUID = -2234911362621554206L;
	
	private final T object;
	private final List<Trait<T>> traits;
	
	private Traits (final T object, final List<Trait<T>> traits) {
		
		this.object = object;
		this.traits = traits;
	}
	
	public static <CLS> Traits<CLS> create (final CLS object, final Collection<Trait<CLS>> traits) {
		Assert.notNull (object, "object");

		return new Traits<CLS> (object, traits == null || traits.isEmpty () ? Collections.<Trait<CLS>>emptyList () : new ArrayList<> (traits));
	}
	
	public static <CLS> Traits<CLS> create (final CLS object) {
		return create (object, Collections.<Trait<CLS>>emptyList ());
	}
	
	public T get () {
		return object;
	}
	
	public <CLS extends Trait<T>> boolean has (final Class<CLS> cls) {
		if (cls.isAssignableFrom (object.getClass ())) {
			return true;
		}
		
		for (final Trait<T> trait: traits) {
			if (cls.isAssignableFrom (trait.getClass ())) {
				return true;
			}
		}
		
		return false;
	}
	
	public Collection<Trait<T>> traits () {
		return Collections.unmodifiableCollection (traits);
	}
	
	public <CLS extends Trait<T>> Collection<CLS> traits (final Class<CLS> cls) {
		final ArrayList<CLS> matches = new ArrayList<> (10);
		
		if (cls.isAssignableFrom (object.getClass ())) {
			@SuppressWarnings("unchecked")
			final CLS instance = (CLS)object;
			
			matches.add (instance);
		}
		
		for (final Trait<T> trait: traits) {
			if (cls.isAssignableFrom (trait.getClass())) {
				@SuppressWarnings("unchecked")
				final CLS instance = (CLS)trait;
				
				matches.add (instance);
			}
		}
		
		return Collections.unmodifiableCollection (matches);
	}
	
	public <CLS extends Trait<T>> CLS trait (final Class<CLS> cls) {
		final Collection<CLS> matches = traits (cls);
		
		if (matches.isEmpty ()) {
			return null;
		}
		
		if (matches.size () > 1) {
			throw new IllegalStateException ("Multiple traits found that match: " + cls.getClass ().getCanonicalName ());
		}
		
		return matches.iterator ().next ();
	}
	
	public Traits<T> with (final Trait<T> trait) {
		final ArrayList<Trait<T>> newTraits = new ArrayList<> (traits);
		
		newTraits.add (trait);
		
		return new Traits<T> (object, newTraits);
	}
}
