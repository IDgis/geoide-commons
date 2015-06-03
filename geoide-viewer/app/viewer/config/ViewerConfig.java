package viewer.config;

import geoide.config.ActorConfig;
import geoide.config.ControllerConfig;
import geoide.config.MapViewConfig;
import geoide.config.PrintServiceConfig;
import geoide.config.RemoteConfig;
import geoide.config.StreamConfig;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.commons.domain.provider.MapProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import controllers.viewer.Viewer;


@Configuration
@Import ({
	MapConfig.class,
	ControllerConfig.class,
	ActorConfig.class,
	PrintServiceConfig.class,
	StreamConfig.class,
	MapViewConfig.class,
	RemoteConfig.class
})
public class ViewerConfig {
	@Autowired
	@Bean
	public Viewer viewerController (MapProvider mapPovider, TableOfContents toc) {
		return new Viewer (mapPovider, toc);
	}
}