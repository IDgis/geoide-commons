package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;

import org.jsoup.nodes.Element;
/**
* a report block stores a filled html snippet and a related css URI
**/ 



public class Block {
	private final Element block;
	private final URI cssUri;
	
	/**
	* constructs a report block object
	* @param block 	a filled html snippet (report block)
	* @param css  	the URI to the related block css in the document store
	* 
	**/ 
	public Block(Element block, URI cssUri) {
		this.block = block;
		this.cssUri = cssUri;
				
	}

	public Element getBlock() {
		return block;
	}

	public URI getCssUri() {
		return cssUri;
	}
	
	

}
