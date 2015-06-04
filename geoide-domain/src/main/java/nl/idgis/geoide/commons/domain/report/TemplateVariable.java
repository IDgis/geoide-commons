package nl.idgis.geoide.commons.domain.report;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonGetter;

public class TemplateVariable implements Serializable {
	private static final long serialVersionUID = -7001444877276405473L;
	
	private final String name;
	private final int maxwidth;
	private final String defaultValue;
	
	public TemplateVariable (final String name, final String defaultValue, final int maxwidth) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.maxwidth = maxwidth;
	}

	public String getName () {
		return name;
	}

	public int getMaxwidth () {
		return maxwidth;
	}

	@JsonGetter ("default")
	public String getDefaultValue () {
		return defaultValue;
	}
}
