package viewer.config;

import geoide.config.ActorConfig;
import geoide.config.ControllerConfig;
import geoide.config.HttpClientConfig;
import geoide.config.LayerTypeConfig;
import geoide.config.PrintServiceConfig;
import geoide.config.ServiceTypeConfig;
import geoide.config.StreamConfig;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.domain.traits.spring.TypedTrait;
import nl.idgis.geoide.commons.layer.DefaultLayerType;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.toc.TOCdefaultLayerTypeTrait;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.service.wms.WMSServiceType;
import nl.idgis.geoide.service.wms.toc.TOCwmsTrait;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
	TOCConfig.class,
	LayerTypeConfig.class,
	PrintServiceConfig.class,
	StreamConfig.class,
	HttpClientConfig.class
})
public class ViewerConfig {
	@Autowired
	@Bean
	public Viewer viewerController (MapProvider mapPovider, TOC toc) {
		return new Viewer (mapPovider, toc);
	}
	
	@Autowired
	@Bean
	@Qualifier ("layerTypeTrait")
	public TypedTrait<LayerType, DefaultLayerType> tocDefaultLayerTypeTrait (ServiceTypeRegistry serviceTypeRegistry) {
		return TypedTrait.create(DefaultLayerType.class, new TOCdefaultLayerTypeTrait(serviceTypeRegistry));	
	}
	
	@Autowired
	@Bean
	@Qualifier ("serviceTypeTrait")
	public TypedTrait<ServiceType, WMSServiceType> tocWmsTrait () {
		return TypedTrait.create(WMSServiceType.class, new TOCwmsTrait());
	}
}