package nl.idgis.planoview.util;

public class Assert {

	public static void notNull (final Object value, final String name) {
		if (value == null) {
			throw new NullPointerException (String.format ("%s cannot be null", name));
		}
	}
}
