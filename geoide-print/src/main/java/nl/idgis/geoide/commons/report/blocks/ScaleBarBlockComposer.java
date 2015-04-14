package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.ogc.util.MimeContentType;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import play.libs.F.Function;
import play.libs.F.Promise;

public class ScaleBarBlockComposer implements BlockComposer {

	@Override
	public Promise<Block> compose(Element blockElement, BlockInfo info,	DocumentCache documentCache) throws Throwable {
		ScaleBarBlockInfo barinfo = (ScaleBarBlockInfo) info;
		//Element scaleBar = blockElement.appendElement("div");
		
		//create svg file
		URI scaleBarUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		Document scaleBarDoc = new Document(scaleBarUri.toString());
		Element svgNode = scaleBarDoc.appendElement("svg"); 
		
		svgNode	//.attr ("viewBox", "0 0 " + info.getWidthpx() + " " + info.getHeightpx() + "")
				.attr ("width", (barinfo.getTotalWidthmm()) + "mm" )
				.attr ("height", "10mm")
				.attr ("version", "1.1")
				.attr ("viewbox", "0,0," + (barinfo.getTotalWidthmm()) + ",10")
				.attr ("xmlns","http://www.w3.org/2000/svg")
				.attr ("xmlns:xlink", "http://www.w3.org/1999/xlink");
			
		int x = 5;
		int y = 5;
		String style = "stroke:#000000;stroke-width:0.3;stroke-linecap:butt; fill:" ;
		Element svgText;
		int n = 0;
		for(n = 0; n < barinfo.getNumberOfRects(); n++ ){
			Element svgRect = svgNode.appendElement("rect");
			svgRect.attr("x", "" + x)
					.attr("y","" + y)
					.attr("width",  "" + barinfo.getRectWidthmm())
					.attr("height", "2")
					.attr("style", style + ((n & 1) == 0 ? "#000000" : "#ffffff"));
			svgText = svgNode.appendElement("text");
			svgText.attr("x", "" + x)
					.attr("y","" + (y - 1))
					.attr("style","text-anchor: middle;font-size:2mm;font-family:Arial");
			svgText.appendText(barinfo.getScaleBarText(n));
			x += barinfo.getRectWidthmm();
					
		}	
		svgText = svgNode.appendElement("text");
		svgText.attr("x", "" + x)
				.attr("y","" + (y - 1))
				.attr("style","text-anchor: middle;font-size:2mm;font-family:Arial");
		svgText.appendText(barinfo.getScaleBarText(n));	
		
		
		//create html image object
		Element scaleBar = blockElement.appendElement("div");
		scaleBar.attr("id", "scaleBar_" + info.getBlockAttribute("viewerstate-id"));		
		Element scaleBarObject = scaleBar.appendElement("object");
		scaleBarObject	.attr("type", "image/svg+xml")
						.attr("style", "left:0;top:0;width:" + barinfo.getTotalWidthmm()  + "mm;height:10mm;")
						.attr("data", scaleBarUri.toString())
						.attr("class", "outline");
		
		Promise<nl.idgis.geoide.documentcache.Document> scaleBarPromise = documentCache.store(scaleBarUri, new MimeContentType ("image/svg+xml"), scaleBarDoc.toString().getBytes());
	
		final Block scaleBarBlock = new Block(blockElement, null);
		
		return scaleBarPromise 
				.map ((d) -> scaleBarBlock);
				
				/*.map (new Function<nl.idgis.geoide.documentcache.Document, Block> () {
					@Override
					public Block apply (
							final nl.idgis.geoide.documentcache.Document scaleBarPromise)
							throws Throwable {
						return scaleBarBlock;
					}
				});*/
		
		
	}

}
