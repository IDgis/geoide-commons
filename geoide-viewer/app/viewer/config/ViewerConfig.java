package viewer.config;

import geoide.config.ActorConfig;
import geoide.config.ApiConfig;
import geoide.config.ControllerConfig;
import geoide.config.RemoteConfig;
import geoide.config.StreamConfig;
import nl.idgis.geoide.commons.domain.api.MapProviderApi;
import nl.idgis.geoide.commons.domain.api.TableOfContents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import controllers.viewer.Viewer;

@Configuration
@Import ({
	ControllerConfig.class,
	ActorConfig.class,
	StreamConfig.class,
	RemoteConfig.class,
	ApiConfig.class
})
public class ViewerConfig {
	@Autowired
	@Bean
	public Viewer viewerController (MapProviderApi mapPovider, TableOfContents toc) {
		return new Viewer (mapPovider, toc);
	}
}