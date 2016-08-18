package nl.idgis.geoide.commons.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.provider.ReloadableStaticMapProvider;
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
	 * @return 			A configured {@link ReloadableStaticMapProvider} instance.
	 * @throws			IOException
	 */
	@Bean
	@Autowired
	public ReloadableStaticMapProvider mapProvider (ResourcePatternResolver resourcePatternResolver, final ConfigWrapper config) throws IOException {
		return new ReloadableStaticMapProvider (() -> {
			
			try {
				Stream<Resource> configs = Stream.of (				
					config.getString ("geoide.service.components.mapProvider.resources.maps", "nl/idgis/geoide/commons/config/map/maps.json"),
					config.getString ("geoide.service.components.mapProvider.resources.services", "nl/idgis/geoide/commons/config/map/services.json"),
					config.getString ("geoide.service.components.mapProvider.resources.featureTypes", "nl/idgis/geoide/commons/config/map/featuretypes.json"),
					config.getString ("geoide.service.components.mapProvider.resources.serviceLayers", "nl/idgis/geoide/commons/config/map/servicelayers.json"),
					config.getString ("geoide.service.components.mapProvider.resources.layers", "nl/idgis/geoide/commons/config/map/layers.json"))
						.map (resourcePatternResolver::getResource);
				
				Stream<Resource> allConfigs;
				String configDir = config.getString ("geoide.service.components.mapProvider.configDir", null);
				if (configDir != null) {
					allConfigs = Stream.concat (
						configs,
						Stream.of (resourcePatternResolver.getResources("file:" + configDir + "/*.json")));
				} else {
					allConfigs = configs;
				}
				
				final JsonMapProviderBuilder builder = JsonMapProviderBuilder.create ();
				//TODO: wait
				
				allConfigs
					.map (resource -> replaceJsonVariables (resource, config))
					.forEach (builder::addJson);

				return builder.build ();
			
			} catch (RuntimeException e) {
				throw e;			
			} catch (Exception e) {
				throw new RuntimeException (e);
			}
		});
	}
	
	private JsonNode replaceJsonVariables (final Resource resource, final ConfigWrapper config) {
		log.info ("loading json from: {}", resource);
		
		try {
			final InputStream inputStream = resource.getInputStream ();
			final JsonNode node = JsonFactory.mapper ().readTree (inputStream);
			
			filterNode (node, config);
			
			return node;
		} catch (IOException e) {
			throw new RuntimeException ("Failed to load " + resource, e);
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
