package viewer.config;

import geoide.config.ActorConfig;
import geoide.config.ControllerConfig;
import geoide.config.LayerTypeConfig;
import geoide.config.ServiceTypeConfig;
import nl.idgis.geoide.commons.domain.provider.MapProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import toc.config.TOCConfig;
import controllers.toc.TOC;
import controllers.viewer.Viewer;

@Configuration
@Import ({
	MapConfig.class,
	ControllerConfig.class,
	ActorConfig.class,
	ServiceTypeConfig.class,
	LayerTypeConfig.class,
	TOCConfig.class
})
public class ViewerConfig {
	@Autowired
	@Bean
	public Viewer viewerController (MapProvider mapPovider, TOC toc) {
		return new Viewer (mapPovider, toc);
	}
}