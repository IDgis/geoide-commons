package nl.idgis.geoide.commons.remote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoteMethodCall implements Serializable {
	private static final long serialVersionUID = -2008259033657150829L;
	
	private final Class<?> iface;
	private final String qualifier;
	private final MethodReference methodReference;
	private final List<Object> arguments;
	
	public RemoteMethodCall (final Class<?> iface, final String qualifier, final MethodReference methodReference, final List<Object> arguments) {
		if (iface == null) {
			throw new NullPointerException ("iface cannot be null");
		}
		if (methodReference == null) {
			throw new NullPointerException ("methodReference cannot be null");
		}
		
		this.iface = iface;
		this.methodReference = methodReference;
		this.qualifier = qualifier;
		this.arguments = arguments == null || arguments.isEmpty () ? Collections.emptyList () : new ArrayList<> (arguments);
	}
	
	public Class<?> getInterface () {
		return iface;
	}
	
	public MethodReference getMethodReference () {
		return methodReference;
	}
	
	public List<Object> getArguments () {
		return Collections.unmodifiableList (arguments);
	}
	
	public String getQualifier () {
		return qualifier;
	}
	
	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		builder.append (getInterface().getCanonicalName ());
		builder.append ("#");
		builder.append (getMethodReference ().getName ());
		builder.append ("(");
		
		String separator = "";
		for (final Class<?> cls: getMethodReference ().getParameterTypes()) {
			builder.append (separator);
			builder.append (cls.getCanonicalName ());
			separator = ", ";
		}
		
		builder.append (")");
		
		return builder.toString ();
	}
}
