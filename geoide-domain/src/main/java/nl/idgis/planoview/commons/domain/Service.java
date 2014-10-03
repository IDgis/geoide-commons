package nl.idgis.planoview.commons.domain;

import nl.idgis.planoview.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Service extends Entity {

	private static final long serialVersionUID = -5734836090472502953L;
	
	private final ServiceIdentification identification;
	private final String label;
	
	@JsonCreator
	public Service (
			final @JsonProperty("id") String id,
			final @JsonProperty("label") String label,
			final @JsonProperty("identification") ServiceIdentification identification) {
		super (id);
		
		Assert.notNull (identification, "identification");
		Assert.notNull (label, "label");
		
		this.identification = identification;
		this.label = label;
	}

	public ServiceIdentification getIdentification () {
		return identification;
	}

	public String getLabel () {
		return label;
	}
}
