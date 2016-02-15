package nl.idgis.geoide.commons.remote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serializable container for a remote method call. Contains a reference to the method and
 * a list of arguments.
 */
public class RemoteMethodCall implements Serializable {
	private static final long serialVersionUID = -2008259033657150829L;
	
	private final Class<?> iface;
	private final String qualifier;
	private final MethodReference methodReference;
	private final List<Object> arguments;

	/**
	 * Constructs a new remote method call.
	 * 
	 * @param iface				The service interface the method is provided by. Cannot be null.
	 * @param qualifier			Optional qualifier to identify the implementation of the service interface.
	 * @param methodReference	Reference to the method that is being invoked. Cannot be null.
	 * @param arguments			List of arguments to the method. A null value is interpreted as an empty list.
	 */
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

	/**
	 * @return The service interface that provides the method.
	 */
	public Class<?> getInterface () {
		return iface;
	}
	
	/**
	 * @return A reference to the method that is being invoked.
	 */
	public MethodReference getMethodReference () {
		return methodReference;
	}

	/**
	 * @return The arguments to the method.
	 */
	public List<Object> getArguments () {
		return Collections.unmodifiableList (arguments);
	}
	
	/**
	 * @return An optional qualifier to identify the service implementation. Can be null.
	 */
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
