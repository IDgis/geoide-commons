package controllers.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.webjars.WebJarAssetLocator;
import org.xml.sax.SAXException;

import play.Logger;
import play.libs.Json;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.core.routes;

public class RequireJS {
	
    public static final String WEBJARS_MAVEN_PREFIX = "META-INF/maven/org.webjars";

	public static String getDojoConfig () {
		return getDojoConfig (null, null);
	}
	
	public static String getDojoConfig (final Seq<Tuple2<String, String>> customPackages, final Seq<Tuple2<String, scala.Boolean>> otherConfig) {
		final WebJarAssetLocator locator = new WebJarAssetLocator ();
		final Map<String, String> webJars = locator.getWebJars ();
		final ObjectNode root = Json.newObject ();
		final ArrayNode packages = root.putArray ("packages");
		
		// Add base configuration:
		root.put ("async", true);
		root.put ("parseOnLoad", false);
		
		if (otherConfig != null) {
			for (final Tuple2<String, scala.Boolean> config: JavaConversions.asJavaCollection (otherConfig)) {
				root.put (config._1 (), scala.Boolean.unbox (config._2 ()));
			}
		}

		for (final Map.Entry<String, String> webJar: webJars.entrySet ()) {
			final String rawRequireJsConfig = getRawWebJarRequireJsConfig (webJar);
			final JsonNode requireJsConfig = rawRequireJsConfig.trim ().isEmpty () ? MissingNode.getInstance () : Json.parse (rawRequireJsConfig);
			final JsonNode pathsConfig = requireJsConfig.path ("paths");
			
			if (pathsConfig.isMissingNode ()) {
				continue;
			}
			

			final Iterator<Map.Entry<String, JsonNode>> fieldIterator = pathsConfig.fields ();

			while (fieldIterator.hasNext ()) {
				final Map.Entry<String, JsonNode> field = fieldIterator.next ();
				
				final ObjectNode packageNode = packages.addObject ();
				packageNode.put ("name", field.getKey ());
				
				final String fullPath = field.getValue ().asText ();
				final int offset = fullPath.lastIndexOf ('/');
				final String path = offset >= 0 ? fullPath.substring (0, offset) : "";
						
				packageNode.put ("location", controllers.core.routes.Assets.at ("lib/" + webJar.getKey () + (path.isEmpty () ? "" : "/" + path)).url ());
			}
		}

		if (customPackages != null) {
			for (final Tuple2<String, String> otherPackage: JavaConversions.asJavaCollection (customPackages)) {
				final ObjectNode packageNode = packages.addObject ();
				
				packageNode.put ("name", otherPackage._1 ());
				packageNode.put ("location", otherPackage._2 ());
			}
		}

		final String config = Json.stringify (root);
		if (config.startsWith ("{") && config.endsWith ("}")) {
			return config.substring (1, config.length () - 1);
		} else {
			return config;
		}
	}
	
    private static String getRawWebJarRequireJsConfig(Map.Entry<String, String> webJar) {
        String filename = WEBJARS_MAVEN_PREFIX + "/" + webJar.getKey() + "/pom.xml";
        InputStream inputStream = RequireJS.class.getClassLoader().getResourceAsStream(filename);

        if (inputStream != null) {
            // try to parse: <root><properties><requirejs>{ /* some json */ }</requirejs></properties></root>
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputStream);
                doc.getDocumentElement().normalize();

                NodeList propertiesNodes = doc.getElementsByTagName("properties");
                for (int i = 0; i < propertiesNodes.getLength(); i++) {
                    NodeList propertyNodes = propertiesNodes.item(i).getChildNodes();
                    for (int j = 0; j < propertyNodes.getLength(); j++) {
                        Node node = propertyNodes.item(j);
                        if (node.getNodeName().equals("requirejs")) {
                            return node.getTextContent();
                        }
                    }
                }

            } catch (ParserConfigurationException e) {
                Logger.warn (requireJsConfigErrorMessage (webJar));
            } catch (IOException e) {
                Logger.warn (requireJsConfigErrorMessage (webJar));
            } catch (SAXException e) {
                Logger.warn (requireJsConfigErrorMessage (webJar));
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // what-evs
                }
            }

        } else {
            Logger.warn (requireJsConfigErrorMessage(webJar));
        }

        return "";
    }
    
    private static String requireJsConfigErrorMessage (final Map.Entry<String, String> webJar) {
    	return String.format ("Failed to read configuration for webjar %s %s", webJar.getKey (), webJar.getValue ());
    }
}