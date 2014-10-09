package nl.idgis.geoide.commons.domain.traits;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestTraits {

	public static class A {
		
	}
	
	public interface ATrait extends Trait<A> {
	}
	
	public class FirstTrait implements ATrait {
	}
	
	public class SecondTrait implements ATrait {
	}
	
	public class ThirdTrait extends SecondTrait {
	}
	
	public class FourthTrait implements ATrait {
	}
	
	@Test
	public void testTraits () {
		final A a = new A ();
		final FirstTrait first = new FirstTrait ();
		final SecondTrait second = new SecondTrait ();
		final ThirdTrait third = new ThirdTrait ();
		final Traits<A> traits = Traits.create (a).with (first).with (second).with (third);
		
		assertSame (a, traits.get ());
		assertEquals (3, traits.traits ().size ());
		
		assertTrue (traits.has (FirstTrait.class));
		assertTrue (traits.has (SecondTrait.class));
		assertTrue (traits.has (ThirdTrait.class));
		assertFalse (traits.has (FourthTrait.class));
		
		assertEquals (3, traits.traits (ATrait.class).size ());
		assertEquals (1, traits.traits (FirstTrait.class).size ());
		assertEquals (2, traits.traits (SecondTrait.class).size ());
		assertEquals (1, traits.traits (ThirdTrait.class).size ());
		assertEquals (0, traits.traits (FourthTrait.class).size ());

		assertSame (first, traits.trait (FirstTrait.class));
		assertSame (third, traits.trait (ThirdTrait.class));
		assertNull (traits.trait (FourthTrait.class));
		
		final Set<SecondTrait> t = new HashSet<> (traits.traits (SecondTrait.class));
		
		assertTrue (t.contains (second));
		assertTrue (t.contains (third));
	}
}
