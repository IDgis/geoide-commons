package nl.idgis.geoide.commons.domain;

import nl.idgis.planoview.util.Assert;

public abstract class NamedServiceEntity extends Entity {
	private static final long serialVersionUID = 2694087899306208479L;
	
	private final Service service;
	private final QName name;
	private final String label;

	public NamedServiceEntity (final String id, final Service service, final QName name, final String label) {
		super (id);
		
		Assert.notNull (service, "service");
		Assert.notNull (name, "name");
		Assert.notNull (label, "label");

		this.service = service;
		this.name = name;
		this.label = label;
	}
	
	public Service getService () {
		return service;
	}

	public QName getName () {
		return name;
	}

	public String getLabel () {
		return label;
	}
}
