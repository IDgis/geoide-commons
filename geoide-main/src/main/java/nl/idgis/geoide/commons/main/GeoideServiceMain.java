package nl.idgis.geoide.commons.main;

import java.io.File;

import nl.idgis.geoide.util.ConfigWrapper;
import nl.idgis.geoide.util.GeoideScheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class GeoideServiceMain implements AutoCloseable {
	
	private final static Logger log = LoggerFactory.getLogger (GeoideServiceMain.class);

	final AnnotationConfigApplicationContext applicationContext;
	
	@Configuration
	@ComponentScan (basePackageClasses = nl.idgis.geoide.commons.config.Package.class)
	public static class MainConfig {
		
		/**
		 * Creates the configuration bean by loading the application default configuration ("application.conf" in
		 * the root of the classpath). Additionally the configurations referenced by the config.file and config.resource
		 * system properties are loaded.
		 */
		@Bean
		public Config config () {
			// Load config files:
			log.info ("Loading application default configuration");
			final Config defaultConfig = ConfigFactory.defaultApplication ();
			
			final String configFile = System.getProperty ("config.file", null);
			final Config fileConfig;
			if (configFile != null) {
				log.info ("Loading configuration file: " + configFile);
				fileConfig = ConfigFactory.parseFile (new File (configFile)).withFallback (defaultConfig);
			} else {
				fileConfig = defaultConfig;
			}
			
			final String configResource = System.getProperty ("config.resource", null);
			final Config resourceConfig;
			if (configResource != null) {
				log.info ("Loading config resource: " + configResource);
				resourceConfig = ConfigFactory.load (configResource).withFallback (fileConfig);
			} else {
				resourceConfig = fileConfig;
			}

			return ConfigFactory.defaultOverrides ().withFallback (resourceConfig);
		}
		
		@Bean
		@Autowired
		public ConfigWrapper configWrapper (final Config config) {
			return new ConfigWrapper (config);
		}
	}
	
	public GeoideServiceMain () {
		// Create an application context and register the config file as a bean:
		log.info ("Loading application context");
		applicationContext = new AnnotationConfigApplicationContext (MainConfig.class);
	}
	
	@Override
	public void close () {
		applicationContext.close ();
	}
	
	public void start () {
		Runtime
			.getRuntime ()
			.addShutdownHook (new Thread (() -> {
				log.info ("Shutting down service");
				applicationContext.close ();
			}));
		
		log.info ("Starting service scheduler");
		final GeoideScheduler scheduler = applicationContext.getBean (GeoideScheduler.class);
		scheduler.waitForCompletion ();
	}
	
	public static void main (final String[] args) throws Throwable {
		try (final GeoideServiceMain main = new GeoideServiceMain ()) {
			main.start ();
		}
	}
}
