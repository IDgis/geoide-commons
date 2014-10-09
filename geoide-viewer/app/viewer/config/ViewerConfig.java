package viewer.config;

import controllers.viewer.Viewer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import geoide.config.ActorConfig;
import geoide.config.ControllerConfig;
import geoide.config.ServiceTypeConfig;
import geoide.config.LayerTypeConfig;

@Configuration
@Import ({
	MapConfig.class,
	ControllerConfig.class,
	ActorConfig.class,
	ServiceTypeConfig.class,
	LayerTypeConfig.class
})
public class ViewerConfig {
	@Bean
	public Viewer viewerController () {
		return new Viewer ();
	}
}