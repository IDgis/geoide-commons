package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;
import java.util.UUID;

import nl.idgis.geoide.documentcache.DocumentCache;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.F.Promise;

public class ScaleBarBlockComposer implements BlockComposer {

	@Override
	public Promise<Block> compose(Element blockElement, BlockInfo info,	DocumentCache documentCache) throws Throwable {
		// TODO Auto-generated method stub
		Element scaleBar = blockElement.appendElement("div");
		
		URI scaleBarUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		
		//Element layerObject = mapLayer.appendElement("object");
		//layerObject	.attr("type", "image/svg+xml")
			//		.attr("style", "left:0;top:0;width:" + width + "mm; height:" + height + "mm;");	
		
		
		return null;
	}
	
	
	private Document createScaleBarSvg (ScaleBarBlockInfo info, URI scaleBarUri) {	
		
		Document scaleBarSvg = new Document(scaleBarUri.toString());
		
		Element svgNode = scaleBarSvg.appendElement("svg"); 
		
		svgNode	//.attr ("viewBox", "0 0 " + info.getWidthpx() + " " + info.getHeightpx() + "")
				.attr ("width", info.getTotalWidthmm() + "mm" )
				.attr ("height", "10mm")
				.attr ("version", "1.1")
				.attr ("viewbox", "0,0," + info.getTotalWidthmm() + 15 + ",10")
				.attr ("xmlns","http://www.w3.org/2000/svg")
				.attr ("xmlns:xlink", "http://www.w3.org/1999/xlink");
			
		int x = 5;
		int y = 5;
		String style = "stroke:#000000;stroke-width:0.3;stroke-linecap:butt; fill:" ;
		
		
		
		for(int n = 0; n < info.getNumberOfRects(); n++ ){
			Element svgRect = svgNode.appendElement("rect");
			svgRect.attr("x", "" + x)
					.attr("y","" + y)
					.attr("width",  "" + info.getRectWidthmm())
					.attr("style", style + ((n & 1) == 0 ? "#000000" : "#ffffff"));
			Element svgText = svgNode.appendElement("text");
			svgText.attr("x", "" + x)
					.attr("y","" + (y - 1))
					.attr("style","text-anchor: middle;font-size:4px;font-family:Arial");
			svgText.appendText(info.getScaleBarText(n));
			x += info.getRectWidthmm();
					
		}
		
		return scaleBarSvg;
		
	}

}
