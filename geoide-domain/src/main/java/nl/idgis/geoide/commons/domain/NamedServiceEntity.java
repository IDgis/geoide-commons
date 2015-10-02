package nl.idgis.geoide.commons.domain;

import nl.idgis.geoide.util.Assert;

public abstract class NamedServiceEntity extends Entity {
	private static final long serialVersionUID = 2694087899306208479L;
	
	private final Service service;
	private final QName name;
	
	public NamedServiceEntity (final String id, final Service service, final QName name, final String label) {
		super (id, label);
		
		Assert.notNull (service, "service");
		Assert.notNull (name, "name");

		this.service = service;
		this.name = name;
	}
	
	public Service getService () {
		return service;
	}

	public QName getName () {
		return name;
	}

}
