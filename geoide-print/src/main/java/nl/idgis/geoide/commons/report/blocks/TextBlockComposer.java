package nl.idgis.geoide.commons.report.blocks;


import java.net.URI;

import nl.idgis.geoide.commons.report.ReportData;

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
	public Promise<Element> compose(JsonNode blockInfo, Element block, ReportData reportData) {
		//parse blockInfo
		Element textElement = block.appendElement(blockInfo.path("tag").asText());
		textElement.append(blockInfo.path("text").asText()); 
		
		return Promise.pure(block);

	}


	@Override
	public URI getBlockCssUri() {
		return null;
	}
	
	

}
