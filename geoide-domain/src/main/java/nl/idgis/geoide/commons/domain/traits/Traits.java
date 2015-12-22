package nl.idgis.geoide.commons.domain.traits;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.util.Assert;

/**
 * A "traits" object contains one base object and zero or more instances of a {@link Trait} to augment it.
 *
 * @param <T>	The type of object contained in the Traits object.
 */
public final class Traits<T> implements Serializable {
	private static final long serialVersionUID = -2234911362621554206L;
	
	private final T object;
	private final List<Trait<T>> traits;
	
	/**
	 * Constructs a new instance by providing an object and a list of traits.
	 * 
	 * @param object	The main object in the traits object.
	 * @param traits	The additional traits.
	 */
	private Traits (final T object, final List<Trait<T>> traits) {
		
		this.object = object;
		this.traits = traits;
	}

	/**
	 * Creates a new Traits instance by providing a base object and a list of additional traits.
	 * 
	 * @param object	The base object.
	 * @param traits	The optional list of additional traits. A null value is interpreted as an empty list.
	 * @return			The resulting Traits instance.
	 */
	public static <CLS> Traits<CLS> create (final CLS object, final Collection<Trait<CLS>> traits) {
		Assert.notNull (object, "object");

		return new Traits<CLS> (object, traits == null || traits.isEmpty () ? Collections.<Trait<CLS>>emptyList () : new ArrayList<> (traits));
	}

	/**
	 * Creates a traits object by providing only a base object.
	 * 
	 * @param object	The base object for the traits object.
	 * @return			The new traits object.
	 */
	public static <CLS> Traits<CLS> create (final CLS object) {
		return create (object, Collections.<Trait<CLS>>emptyList ());
	}
	
	/**
	 * Returns the contained object.
	 *  
	 * @return	The contained object.
	 */
	public T get () {
		return object;
	}
	
	/**
	 * Returns true if this object contains a trait of the given type.
	 * 
	 * @param cls	The type to test for (cannot be null).
	 * @return		True if this object contains a trait of the given type.
	 */
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
	
	/**
	 * Returns a collection of additional traits.
	 * 
	 * @return	A collection of additional traits. Returns an empty collection if there are no traits.
	 */
	public Collection<Trait<T>> traits () {
		return Collections.unmodifiableCollection (traits);
	}

	/**
	 * Returns all traits of the given type that are contained in this object.
	 * 
	 * @param cls	The class of the returned traits objects.
	 * @return		A collection of traits objects that are of the given type.
	 */
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
	
	/**
	 * Returns a single trait of the given type, or null if such a trait does not exist.
	 * 
	 * @param cls						The class of the trait to return.
	 * @return							The trait instance, or null if it doesn't exist.
	 * @throws IllegalStateException	Thrown if the object contains multiple traits of the given type.
	 */
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
	
	/**
	 * Returns a new Traits object that contains the information from this object and adds the given trait.
	 * 
	 * @param trait	The trait to add, cannot be null.
	 * @return		A new traits object containing the additional trait as well as the information from this object.
	 */
	public Traits<T> with (final Trait<T> trait) {
		final ArrayList<Trait<T>> newTraits = new ArrayList<> (traits);
		
		newTraits.add (trait);
		
		return new Traits<T> (object, newTraits);
	}
}
