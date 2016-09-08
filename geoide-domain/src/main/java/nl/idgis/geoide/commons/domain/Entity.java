package nl.idgis.geoide.commons.domain;

import java.io.Serializable;

import nl.idgis.geoide.util.Assert;

public abstract class Entity implements Serializable {

	private static final long serialVersionUID = 545601754396146175L;
	
	private final String id;
	private final String label;

	public Entity (final String id, String label) {
		Assert.notNull (id, "id");
		//Assert.notNull (label, "label");
		
		this.id = id;
		this.label = label;
		
	}

	public String getId () {
		return id;
	}
	
	public String getLabel () {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Entity other = (Entity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
