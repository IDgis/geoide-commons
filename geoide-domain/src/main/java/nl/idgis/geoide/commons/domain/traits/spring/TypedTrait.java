package nl.idgis.geoide.commons.domain.traits.spring;

import java.util.ArrayList;
import java.util.Collection;

import nl.idgis.geoide.commons.domain.traits.Trait;
import nl.idgis.geoide.commons.domain.traits.Traits;

public final class TypedTrait<BaseType, TargetType extends BaseType> {
	private final Class<TargetType> type;
	private final Trait<BaseType> trait;
	
	private TypedTrait (final Class<TargetType> type, final Trait<BaseType> trait) {
		this.type = type;
		this.trait = trait;
	}
	
	public static <BT, TT extends BT> TypedTrait<BT, TT> create (final Class<TT> type, final Trait<BT> trait) {
		return new TypedTrait<BT, TT> (type, trait);
	}

	public Class<TargetType> getType () {
		return type;
	}

	public Trait<BaseType> getTrait () {
		return trait;
	}
	
	public static <T> Traits<T> makeTraits (final T object, final Collection<TypedTrait<?, ?>> traits) {
		final Class<? extends Object> cls = object.getClass ();
		final ArrayList<Trait<T>> matches = new ArrayList<> ();

		for (final TypedTrait<?, ?> trait: traits) {
			if (trait.getType ().equals (cls)) {
				@SuppressWarnings("unchecked")
				final Trait<T> t = (Trait<T>)trait.getTrait ();
				matches.add (t);
			}
		}
		
		return Traits.<T>create (object, matches);
	}
}
