package nl.idgis.geoide.commons.report.blocks;

import java.util.Set;

import org.jsoup.nodes.Element;

public abstract class BlockUtil {
	
	public static int getGridWidth(Element element) {
		int gridWidth = 1;
		Set <String> classNames = element.classNames();	
		if (element.hasClass("grid-col")){
			for (String className : classNames) {
				if (className.matches("^span\\-[1-9][0-9]*$")) {
					return Integer.parseInt(className.substring(5));
				}
			}		
		} else {
			for (String className : classNames) {
				if (className.matches("^col-span\\-[1-9][0-9]*$")) {
					return Integer.parseInt(className.substring(9));
				} 
			}	
			if(element.parent() != null) { 
				return getGridWidth (element.parent());
			}	
		}
		
		return gridWidth;
	}	
	
	public static int getGridHeight(Element element) {
		int gridHeight = 1;
		Set <String> classNames = element.classNames();	
		if (element.hasClass("grid-row")){
			for (String className : classNames) {
				if (className.matches("^span\\-[1-9][0-9]*$")){
					return Integer.parseInt(className.substring(5));
				}
			}		
		} else {
			for (String className : classNames) {
				if (className.matches("^row-span\\-[1-9][0-9]*$")) {
					return Integer.parseInt(className.substring(9));
				} 
			}	
			if(element.parent() != null) { 
				return getGridHeight (element.parent());
			}
		}
		
		return gridHeight;
	}	
		
		
	
	
	

}
