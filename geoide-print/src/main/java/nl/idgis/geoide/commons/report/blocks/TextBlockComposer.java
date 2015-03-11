package nl.idgis.geoide.commons.report.blocks;


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
	 * @param reportData 	object containing some general reportdata such as width and height of a report page 
	 * @return A promise (block object) that will resolve to the resulting element and a related css.
	 **/
	
	@Override
	public Promise<Block> compose(JsonNode blockInfo, Element block, ReportData reportData) {
		//parse blockInfo
		Element textElement = block.appendElement(blockInfo.path("tag").asText());
		textElement.append(blockInfo.path("text").asText()); 
		Block textBlock = new Block(block, null);
		
		return Promise.pure(textBlock);

	}

	

}
