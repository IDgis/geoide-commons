package viewer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import ({
	MapConfig.class,
	ControllerConfig.class,
	ActorConfig.class,
	ServiceTypeConfig.class,
	LayerTypeConfig.class
})
public class ViewerConfig {

}
