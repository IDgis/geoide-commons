package nl.idgis.geoide.commons.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.provider.StaticMapProvider;
import nl.idgis.geoide.map.provider.JsonMapProviderBuilder;
import nl.idgis.geoide.util.ConfigWrapper;

@Configuration
public class ProviderConfig {

	private final static Logger log = LoggerFactory.getLogger (ProviderConfig.class);
	
	/**
	 * Configures and creates a {@link StaticMapProvider} instance. Configuration is loaded
	 * from the classpath, from the resources pointed to by the following configuration
	 * parameters:
	 * 
	 * - geoide.service.components.mapProvider.resources.map
	 * - geoide.service.components.mapProvider.resources.services
	 * - geoide.service.components.mapProvider.resources.featureTypes
	 * - geoide.service.components.mapProvider.resources.serviceLayers
	 * - geoide.service.components.mapProvider.resources.layers
	 * - geoide.service.components.mapProvider.resources.searchtemplates
	 * 
	 * The configuration is overridden by all JSON files found in the directory pointed to
	 * by the property geoide.service.components.mapProvider.configDir 
	 * 
	 * @param config	The application configuration.
	 * @return 			A configured {@link StaticMapProvider} instance.
	 * @throws			IOException
	 */
	@Bean
	@Autowired
	public StaticMapProvider mapProvider (final ConfigWrapper config) throws IOException {
		final String mapsResource = config.getString ("geoide.service.components.mapProvider.resources.maps", "nl/idgis/geoide/commons/config/map/maps.json");
		final String servicesResource = config.getString ("geoide.service.components.mapProvider.resources.services", "nl/idgis/geoide/commons/config/map/services.json");
		final String featureTypesResource = config.getString ("geoide.service.components.mapProvider.resources.featureTypes", "nl/idgis/geoide/commons/config/map/featuretypes.json");
		final String serviceLayersResource = config.getString ("geoide.service.components.mapProvider.resources.serviceLayers", "nl/idgis/geoide/commons/config/map/servicelayers.json");
		final String layersResource = config.getString ("geoide.service.components.mapProvider.resources.layers", "nl/idgis/geoide/commons/config/map/layers.json");
		//final String searchTemplatesResource = config.getString ("geoide.service.components.mapProvider.resources.searchtemplates", "nl/idgis/geoide/commons/config/map/searchtemplates.json");
		
		final String mapConfigurationDirectory = config.getString ("geoide.service.components.mapProvider.configDir", null);
		
		final ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
		
		log.info ("Loading static map configuration from classpath");
		log.info ("Maps configuration: " + mapsResource);
		log.info ("Services configuration: " + servicesResource);
		log.info ("Feature types configuration: " + featureTypesResource);
		log.info ("Service layers configuration: " + serviceLayersResource);
		log.info ("Layers configuration: " + layersResource);
		//log.info ("SearchTemplates configuration: " + searchTemplatesResource);

		// Add default configurations from the classpath:
		final JsonMapProviderBuilder builder = JsonMapProviderBuilder.create (
				replaceJsonVariables (mapsResource, cl.getResourceAsStream (mapsResource), config),
				replaceJsonVariables (servicesResource, cl.getResourceAsStream (servicesResource), config),
				replaceJsonVariables (featureTypesResource, cl.getResourceAsStream (featureTypesResource), config),
				replaceJsonVariables (serviceLayersResource, cl.getResourceAsStream (serviceLayersResource), config),
				replaceJsonVariables (layersResource, cl.getResourceAsStream (layersResource), config)
				//replaceJsonVariables (searchTemplatesResource, cl.getResourceAsStream (searchTemplatesResource), config)
			);
		
		// Add override configurations from the filesystem:
		if (mapConfigurationDirectory != null) {
			final File configDir = new File (mapConfigurationDirectory);
			
			log.info ("Overriding map configuration with files from: " + configDir.getAbsolutePath ());

			if (!configDir.exists () || !configDir.isDirectory ()) {
				throw new IllegalArgumentException ("Map configuration directory: " + configDir + " does not exist or is not a directory");
			}
			
			final String[] jsonFiles = configDir.list ((dir, filename) -> filename.toLowerCase ().endsWith (".json"));
			if (jsonFiles == null) {
				throw new IllegalStateException ("Unable to list JSON files in " + mapConfigurationDirectory);
			}
			
			builder.addJson (Arrays
				.stream (jsonFiles)
				.map (filename -> new File (configDir, filename))
				.map (file -> { 
					try { 
						return replaceJsonVariables (file.toString (), new FileInputStream (file), config); 
					} catch (Exception e) { 
						throw new RuntimeException (e); 
					} 
				})
				.collect (Collectors.toList ())
				.toArray (new JsonNode[0]));
		}
		
		return builder.build ();
	}
	
	private JsonNode replaceJsonVariables (final String filename, final InputStream inputStream, final ConfigWrapper config) {
		try {
			final JsonNode node = JsonFactory.mapper ().readTree (inputStream);
			
			filterNode (node, config);
			
			return node;
		} catch (IOException e) {
			throw new RuntimeException ("Failed to load " + filename, e);
		}
	}
	
	private void filterNode (final JsonNode node, final ConfigWrapper config) {
		if (node.isObject ()) {
			for (final String fieldName: StreamSupport
				.stream (Spliterators.spliteratorUnknownSize (node.fieldNames (), Spliterator.ORDERED), false)
				.collect (Collectors.toList ())) {
				final JsonNode child = node.path (fieldName);
				if (child.isTextual ()) {
					((ObjectNode) node).put (fieldName, replaceVariables (child.asText (), config));
				} else {
					filterNode (child, config);
				}
			}
		} else if (node.isArray ()) {
			for (int i = 0; i < node.size (); ++ i) {
				final JsonNode child = node.path (i);
				if (child.isTextual ()) {
					((ArrayNode) node).set (i, TextNode.valueOf (replaceVariables (child.asText (), config)));
				} else {
					filterNode (child, config);
				}
			}
		}
	}
	
	private String replaceVariables (final String value, final ConfigWrapper config) {
		final Pattern pattern = Pattern.compile ("\\$\\{(.+)\\}", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher (value);
		final StringBuilder builder = new StringBuilder ();

		int i = 0;
		while (matcher.find ()) {
			final String replacement = config.getString (matcher.group (1), null);
			
			builder.append (value.substring (i, matcher.start ()));
			
			if (replacement == null) {
				builder.append (matcher.group (0));
			} else {
				builder.append (replacement);
			}
			
			i = matcher.end ();
		}
		
		builder.append (value.substring (i, value.length ()));
		
		return builder.toString ();
	}
}
