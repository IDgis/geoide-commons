package nl.idgis.geoide.commons.report.blocks;


import org.jsoup.nodes.Element;

import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;

public class TextBlockComposer implements BlockComposer {

	/**
	 * A specialized composer component that composes a TextBlock with information from the client 
	 * and a (part of) a report template
	 **/ 
	
	public TextBlockComposer() {
		super();
	};
	
	
	/**
	 * Composes a text report block with information from the client and a (part of) a report template
	 * 
	 * @param blockInfo		a Json node with information from the client.
	 * @param block			(part of) a template to be filled with client info
	 * @return A promise that will resolve to the resulting element.
	 **/
	
	@Override
	public Promise<Element> compose(JsonNode blockInfo, Element block) {
		//tekst en tag uit blockInfo
		String reportText = "<h2>Dit is geen titel</h2>";
		if (block.hasClass("title")) {
			reportText = "<h1>Dit is een titel</h1>";
		} 
		block.append(reportText);
		return Promise.pure(block);

	}
	
	

}
