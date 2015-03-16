package nl.idgis.geoide.commons.report.blocks;


import nl.idgis.geoide.documentcache.DocumentCache;

import org.jsoup.nodes.Element;

import play.libs.F.Promise;

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
	public Promise<Block> compose (Element blockElement, BlockInfo info, DocumentCache documentCache) throws Throwable {
		Element block = getChild(blockElement);
		block.appendElement (((TextBlockInfo) info).getTag()).append (((TextBlockInfo) info).getText());
		Block textBlock = new Block(blockElement, null);
		return Promise.pure(textBlock);
	}
	
	private Element getChild(Element block) {
		if(block.children().size() > 0) {
			block = getChild(block.children().first());
		}
		return block;
	}
}
