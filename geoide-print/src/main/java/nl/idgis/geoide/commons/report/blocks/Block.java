package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;

import org.jsoup.nodes.Element;
/**
* a report block stores a filled html snippet and a related css URI
**/ 



public class Block {
	private final Element block;
	private final URI css;
	
	/**
	* constructs a report block object
	* @param block 	a filled html snippet (report block)
	* @param css  	the URI to the related block css in the document store
	* 
	**/ 
	public Block(Element block, URI css) {
		this.block = block;
		this.css = css;
				
	}

	public Element getBlock() {
		return block;
	}

	public URI getCss() {
		return css;
	}
	
	

}
