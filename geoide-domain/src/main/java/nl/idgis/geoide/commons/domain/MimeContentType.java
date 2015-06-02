package nl.idgis.geoide.commons.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MimeContentType {

	private final String originalString;
	private final String type;
	private final String subType;
	private final Map<String, String> parameters = new HashMap<> ();
	
	public MimeContentType (final String contentType) {
		if (contentType == null) {
			throw new NullPointerException ("contentType cannot be null");
		}
		
		this.originalString = contentType;
		
		final String[] parts = contentType.split ("\\;");
		
		// Type and subtype:
		final String[] typeParts = parts[0].trim ().split ("\\/");
		if (typeParts.length != 2 || typeParts[0].trim ().isEmpty () || typeParts[1].trim ().isEmpty ()) {
			throw new IllegalArgumentException ("Content type must contain a type and subtype");
		}
		type = typeParts[0].trim ().toLowerCase ();
		subType = typeParts[1].trim ().toLowerCase ();
		
		// Parameters:
		for (int i = 1; i < parts.length; ++ i) {
			final String[] parameterParts = parts[i].trim ().split ("\\=");
			if (parameterParts.length != 2) {
				throw new IllegalArgumentException ("Invalid parameter");
			}
			if (parameterParts[0].trim ().isEmpty ()) {
				throw new IllegalArgumentException ("Parameter without a name");
			}
			
			final String value = parameterParts[1].trim ();
			if (value.charAt (0) == '\"' && value.charAt (value.length () - 1) == '\"') {
				parameters.put (parameterParts[0].trim ().toLowerCase (), value.substring (1, value.length () - 1).toLowerCase ());
			} else {
				parameters.put (parameterParts[0].trim ().toLowerCase (), value.toLowerCase ());
			}
		}
	}
	
	public String original () {
		return originalString;
	}
	
	public String type () {
		return this.type;
	}
	
	public String subType () {
		return this.subType;
	}
	
	public Map<String, String> parameters () {
		return Collections.unmodifiableMap (parameters);
	}
	
	public boolean matches (final MimeContentType other) {
		return matches (other, true);
	}
	
	private boolean matches (final MimeContentType other, final boolean useWildcards) {
		// If the type and subtype don't match, the content type is never equal:
		if (!type ().equals (other.type ())) {
			return false;
		}
		if (useWildcards) {
			if ( !"*".equals (subType ()) && !"*".equals (other.subType ()) && !subType ().equals (other.subType ())) {
				return false;
			}
		} else if (!subType ().equals (other.subType ())) {
			return false;
		}
		
		// Match the parameters:
		final Map<String, String> parameters = parameters ();
		final Map<String, String> otherParameters = other.parameters ();
		
		// Parameter lists must have the same size:
		if (parameters.size () != otherParameters.size ()) {
			return false;
		}

		// All parameters must be equal:
		for (final String key: parameters.keySet ()) {
			if (!otherParameters.containsKey (key)) {
				return false;
			}
			
			final String a = parameters.get (key);
			final String b = otherParameters.get (key);
			
			if (a == null && b != null) {
				return false;
			}
			if (!a.toLowerCase ().equals (b == null ? null : b.toLowerCase ())) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean matches (final String other) {
		return matches (new MimeContentType (other));
	}
	
	public static boolean isValid (final String contentType) {
		try {
			new MimeContentType (contentType);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		builder.append (type ());
		builder.append ("/");
		builder.append (subType ());
		
		for (final Map.Entry<String, String> entry: parameters ().entrySet ()) {
			builder.append ("; ");
			builder.append (entry.getKey ());
			builder.append ("=\"");
			builder.append (entry.getValue ());
			builder.append ("\"");
		}
		
		return builder.toString ();
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + parametersHashCode ();
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
	private int parametersHashCode () {
		final int prime = 31;
		int result = 1;
		
		for (final Map.Entry<String, String> entry: parameters ().entrySet ()) {
			result = prime * result + (entry.getKey () == null ? 0 : entry.getKey ().toLowerCase ().hashCode ());
			result = prime * result + (entry.getValue () == null ? 0 : entry.getValue ().toLowerCase ().hashCode ());
		}
		
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final MimeContentType other = (MimeContentType) obj;
		
		return matches (other, false);
	}
}
