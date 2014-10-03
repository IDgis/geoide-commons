package nl.idgis.geoide.commons.domain;

import java.io.Serializable;

import nl.idgis.planoview.util.Assert;

public abstract class Entity implements Serializable {

	private static final long serialVersionUID = 545601754396146175L;
	
	private final String id;

	public Entity (final String id) {
		Assert.notNull (id, "id");
		
		this.id = id;
	}

	public String getId () {
		return id;
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
