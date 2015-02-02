package nl.idgis.geoide.commons.print.svg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

public class SVGReplacedElementFactory implements ReplacedElementFactory {
	private final static Logger log = LoggerFactory.getLogger (SVGReplacedElementFactory.class);
	
    @Override
    public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, 
            UserAgentCallback uac, int cssWidth, int cssHeight) {
        Element element = box.getElement();
        log.trace (element.getLocalName ());
        
        if (isSVGElement (element)) {
        	log.trace ("Before getXMLResource");
        	final byte[] bytes = uac.getBinaryResource (element.getAttribute ("data"));
        	log.trace ("After getXMLResource");
        	
        	return new SVGReplacedElement (bytes, cssWidth, cssHeight);
        } /*else if("svg".equals(element.getLocalName ())) {

        	log.trace ("Replacing SVG box");
        	
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder;

            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
            Document svgDocument = documentBuilder.newDocument();
            Element svgElement = (Element) svgDocument.importNode(element, true);
            svgDocument.appendChild(svgElement);
            return new SVGReplacedElement(svgDocument, cssWidth, cssHeight);
        }*/
        return null;
    }
    
    private static boolean isSVGElement (final Element element) {
    	if (!"object".equals (element.getLocalName ())) {
    		return false;
    	}
    	
    	if (!element.hasAttribute ("type") || !"image/svg+xml".equals (element.getAttribute ("type"))) {
    		return false;
    	}
    	
    	if (!element.hasAttribute ("data")) {
    		return false;
    	}
    	
    	return true;
    }

    @Override
    public void reset() {
    }

    @Override
    public void remove (Element e) {
    }

    @Override
    public void setFormSubmissionListener(FormSubmissionListener listener) {
    }
}
