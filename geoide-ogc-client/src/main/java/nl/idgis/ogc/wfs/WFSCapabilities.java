package nl.idgis.ogc.wfs;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.idgis.ogc.util.MimeContentType;
import nl.idgis.services.Capabilities;
import nl.idgis.services.OGCCapabilities;

public class WFSCapabilities extends OGCCapabilities {
	private static final long serialVersionUID = -3434494100170036184L;
	
	private final String version;
	private final ServiceIdentification serviceIdentification;
	private final Map<OperationType, Operation> operations;
	private final Map<QName, FeatureType> featureTypes;

	public WFSCapabilities (final String version, final ServiceIdentification serviceIdentification, final Collection<Operation> operations, final Collection<FeatureType> featureTypes) {
		if (version == null) {
			throw new NullPointerException ("version cannot be null");
		}
		if (serviceIdentification == null) {
			throw new NullPointerException ("serviceIdentification cannot be null");
		}
		if (operations == null) {
			throw new NullPointerException ("operations cannot be null");
		}
		if (featureTypes == null) {
			throw new NullPointerException ("featureTypes cannot be null");
		}
		
		this.version = version;
		this.serviceIdentification = serviceIdentification;
		
		this.operations = new HashMap<OperationType, Operation> ();
		for (final Operation operation: operations) {
			this.operations.put (operation.operationType (), operation);
		}

		final Set<MimeContentType> globalOutputFormats;
		if (this.operations.containsKey (OperationType.GET_FEATURE)) {
			globalOutputFormats = this.operations.get (OperationType.GET_FEATURE).outputFormats ();
		} else {
			globalOutputFormats = null;
		}
		
		this.featureTypes = new HashMap<QName, FeatureType> ();
		for (final FeatureType featureType: featureTypes) {
			final FeatureType featureTypeWithFormats;
			if (featureType.outputFormats().isEmpty ()) {
				featureTypeWithFormats = featureType.withOutputFormats (globalOutputFormats);
			} else {
				featureTypeWithFormats = featureType;
			}
			
			this.featureTypes.put (new QName (featureTypeWithFormats.namespaceUri (), featureTypeWithFormats.name ()), featureTypeWithFormats);
		}
	}
	
	@Override
	public String version () {
		return this.version;
	}
	
	@Override
	public ServiceIdentification serviceIdentification () {
		return this.serviceIdentification;
	}
	
	@Override
	public Collection<Operation> operations () {
		return Collections.unmodifiableCollection (operations.values ());
	}
	
	@Override
	public Operation operation (final OGCCapabilities.OperationType operationType) {
		if (!(operationType instanceof OperationType)) {
			return null;
		}
		
		return operations.get ((OperationType)operationType);
	}
	
	@Override
	public boolean hasOperation (final OGCCapabilities.OperationType operationType) {
		if (!(operationType instanceof OperationType)) {
			return false;
		}
		
		return operations.containsKey ((OperationType)operationType);
	}
	
	@Override
	public Operation operationByName (final String operationName) {
		final OperationType ot = OperationType.byName (operationName);
		
		return operation (ot);
	}
	
	public Collection<FeatureType> featureTypes () {
		return Collections.unmodifiableCollection (featureTypes.values ());
	}
	
	public FeatureType featureType (final String namespaceUri, final String localName) {
		return featureTypes.get (new QName (namespaceUri, localName));
	}
	
	@Override
	public Collection<FeatureType> layers () {
		return featureTypes ();
	}
	
	@Override
	public Collection<FeatureType> allLayers () {
		return featureTypes ();
	}
	
	@Override
	public boolean hasLayer (final String layer) {
		return layer (layer) != null;
	}
		
	@Override
	public FeatureType layer (final String layer) {
		for (final FeatureType featureType: featureTypes ()) {
			if (featureType.name ().equals (layer)) {
				return featureType;
			}
		}
		
		return null;
	}
	
	public static class ServiceIdentification implements Capabilities.ServiceIdentification {
		private static final long serialVersionUID = 3015791761287964002L;
		
		private final String title;
		private final String abstractText;
		private final Set<String> versions;
		
		public ServiceIdentification (final String title, final String abstractText, final Collection<String> versions) {
			if (title == null) {
				throw new NullPointerException ("title cannot be null");
			}
			if (versions == null) {
				throw new NullPointerException ("versions cannot be null");
			}
			if (versions.isEmpty ()) {
				throw new IllegalArgumentException ("At least one version must be specified");
			}
			
			this.title = title;
			this.abstractText = abstractText;
			this.versions = new HashSet<String> (versions);
		}
		
		@Override
		public String title () {
			return this.title;
		}
		
		@Override
		public String abstractText () {
			return this.abstractText;
		}
		
		public Set<String> versions () {
			return Collections.unmodifiableSet (versions);
		}
		
		public boolean hasVersion (final String version) {
			return versions.contains (version);
		}
	}
	
	public static enum OperationType implements OGCCapabilities.OperationType {
		GET_CAPABILITIES ("GetCapabilities"),
		DESCRIBE_FEATURE_TYPE ("DescribeFeatureType"),
		GET_FEATURE ("GetFeature");
		
		private final String name;
		
		OperationType (final String name) {
			this.name = name;
		}
		
		@Override
		public String operationName () {
			return this.name;
		}
		
		public static OperationType byName (final String name) {
			if (name == null) {
				return null;
			}
			
			final String lcName = name.toLowerCase ();
			
			for (final OperationType ot: OperationType.values ()) {
				if (ot.operationName ().toLowerCase ().equals (lcName)) {
					return ot;
				}
			}
			
			return null;
		}
	}
	
	public static class Operation implements OGCCapabilities.Operation {
		private static final long serialVersionUID = 475815839596012738L;
		
		private final OperationType operationType;
		private final String httpGet;
		private final String httpPost;
		private final Set<MimeContentType> outputFormats;
		
		public Operation (final OperationType operationType, final String httpGet, final String httpPost, final Set<MimeContentType> outputFormats) {
			if (operationType == null) {
				throw new NullPointerException ("operationType cannot be null");
			}
			if (httpGet == null && httpPost == null) {
				throw new NullPointerException ("Either httpGet or httpPost must have a value");
			}
			
			this.operationType = operationType;
			this.httpGet = httpGet;
			this.httpPost = httpPost;
			this.outputFormats = outputFormats == null ? Collections.<MimeContentType>emptySet () : new HashSet<> (outputFormats);
		}
		
		@Override
		public OperationType operationType () {
			return this.operationType;
		}
		
		@Override
		public String httpGet () {
			return this.httpGet;
		}
		
		@Override
		public String httpPost () {
			return this.httpPost;
		}
		
		public boolean hasHttpGet () {
			return httpGet () != null;
		}
		
		public boolean hasHttpPost () {
			return httpPost () != null;
		}
		
		public Set<MimeContentType> outputFormats () {
			return Collections.unmodifiableSet (this.outputFormats);
		}
	}
	
	private static class QName implements Serializable {
		private static final long serialVersionUID = 7982643053702449335L;
		
		public final String namespace;
		public final String localName;
		
		public QName (final String namespace, final String localName) {
			this.namespace = namespace;
			this.localName = localName;
		}

		@Override
		public int hashCode () {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((localName == null) ? 0 : localName.hashCode());
			result = prime * result
					+ ((namespace == null) ? 0 : namespace.hashCode());
			return result;
		}

		@Override
		public boolean equals (final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			QName other = (QName) obj;
			if (localName == null) {
				if (other.localName != null)
					return false;
			} else if (!localName.equals(other.localName))
				return false;
			if (namespace == null) {
				if (other.namespace != null)
					return false;
			} else if (!namespace.equals(other.namespace))
				return false;
			return true;
		}
	}
	
	public static class FeatureType implements Capabilities.Layer {
		private static final long serialVersionUID = 6874459420886876901L;
		
		private final String name;
		private final String namespaceUri;
		private final String namespacePrefix;
		private final String title;
		private final String crs;
		private final Set<String> otherCrs;
		private final Set<MimeContentType> outputFormats;
		
		public FeatureType (final String name, final String namespaceUri, final String namespacePrefix, final String title, final String crs, final Collection<String> otherCrs, final Collection<MimeContentType> outputFormats) {
			if (name == null) {
				throw new NullPointerException ("name cannot be null");
			}
			if (crs == null) {
				throw new NullPointerException ("crs cannot be null");
			}
			
			this.name = name;
			this.namespaceUri = namespaceUri;
			this.namespacePrefix = namespacePrefix;
			this.title = title == null ? name : title;
			this.crs = crs;
			this.otherCrs = otherCrs == null ? null : new HashSet<String> (otherCrs);
			this.outputFormats = outputFormats == null ? null : new HashSet<MimeContentType> (outputFormats);
		}
		
		public FeatureType withOutputFormats (final Set<MimeContentType> outputFormats) {
			return new FeatureType (name, namespaceUri, namespacePrefix, title, crs, otherCrs, outputFormats);
		}
		
		@Override
		public String name () {
			return this.name;
		}
		
		public String namespaceUri () {
			return this.namespaceUri;
		}
		
		public String namespacePrefix () {
			return this.namespacePrefix;
		}
		
		@Override
		public String title () {
			return this.title;
		}
		
		public String crs () {
			return this.crs;
		}
		
		public Set<String> otherCrs () {
			return this.otherCrs == null ? Collections.<String>emptySet () : Collections.unmodifiableSet (this.otherCrs);
		}
		
		@Override
		public Set<String> crss () {
			final Set<String> crss = new HashSet<> ();
			
			crss.add (crs ());
			crss.addAll (otherCrs ());
			
			return Collections.unmodifiableSet (crss);
		}
		
		@Override
		public Collection<FeatureType> layers () {
			return Collections.emptyList ();
		}
		
		public Set<MimeContentType> outputFormats () {
			return this.outputFormats == null ? Collections.<MimeContentType>emptySet () : Collections.unmodifiableSet (this.outputFormats);
		}
		
		public boolean supports (final MimeContentType contentType) {
			for (final MimeContentType ct: outputFormats ()) {
				if (ct.matches (contentType)) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public boolean supportsCRS (final String crs) {
			if (crs == null || crs.isEmpty ()) {
				return false;
			}
			
			if (crs.toLowerCase ().equals (this.crs.toLowerCase ())) {
				return true;
			}
			
			for (final String c: otherCrs) {
				if (crs.toLowerCase ().equals (c.toLowerCase ())) {
					return true;
				}
			}
			
			return false;
		}
	}
}
