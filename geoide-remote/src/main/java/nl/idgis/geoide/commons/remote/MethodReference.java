package nl.idgis.geoide.commons.remote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MethodReference implements Serializable {
	private static final long serialVersionUID = 8463842538979402312L;
	
	private final Class<?> cls;
	private final String name;
	private final ArrayList<Class<?>> parameterTypes;
	
	public MethodReference (final Class<?> cls, final String name, final List<Class<?>> parameterTypes) {
		if (cls == null) {
			throw new NullPointerException ("cls cannot be null");
		}
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		if (parameterTypes == null) {
			throw new NullPointerException ("parameterTypes cannot be null");
		}
		
		this.cls = cls;
		this.name = name;
		this.parameterTypes = new ArrayList<Class<?>> (parameterTypes);
	}
	
	public Class<?> getCls () {
		return cls;
	}
	
	public String getName () {
		return  name;
	}
	
	public List<Class<?>> getParameterTypes () {
		return Collections.unmodifiableList (parameterTypes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cls == null) ? 0 : cls.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodReference other = (MethodReference) obj;
		if (cls == null) {
			if (other.cls != null)
				return false;
		} else if (!cls.equals(other.cls))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameterTypes == null) {
			if (other.parameterTypes != null)
				return false;
		} else if (!parameterTypes.equals(other.parameterTypes))
			return false;
		return true;
	}
	
	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		for (final Class<?> pt: parameterTypes) {
			if (builder.length () > 0) {
				builder.append (",");
			}
			builder.append (pt.getCanonicalName ());
		}
		
		return cls.getCanonicalName () + "#" + name + "(" + builder.toString() + ")";
	}
}
