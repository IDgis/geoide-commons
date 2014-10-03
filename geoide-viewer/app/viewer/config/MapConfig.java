package viewer.config;

import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.domain.provider.StaticMapProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import play.Play;

@Configuration
public class MapConfig {
	
	@Bean
	public MapProvider mapProvider () {
		return new StaticMapProvider (Play.application().resourceAsStream ("viewer/map-definition.json"));
	}
}
