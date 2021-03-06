package nl.idgis.geoide.commons.domain;

import nl.idgis.geoide.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Service extends Entity {

	private static final long serialVersionUID = -5734836090472502953L;
	
	private final ServiceIdentification identification;
	private final String printFormat;
	
	@JsonCreator
	public Service (
			final @JsonProperty("id") String id,
			final @JsonProperty("label") String label,
			final @JsonProperty("printFormat") String printFormat,
			final @JsonProperty("identification") ServiceIdentification identification) {
		super (id, label);
		
		Assert.notNull (identification, "identification");
		this.identification = identification;
		this.printFormat = printFormat;
		
	}

	public ServiceIdentification getIdentification () {
		return identification;
	}
	
	public String getPrintFormat () {
		return printFormat;
	}

}
