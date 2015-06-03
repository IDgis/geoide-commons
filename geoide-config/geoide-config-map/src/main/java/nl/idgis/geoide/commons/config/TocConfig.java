package nl.idgis.geoide.commons.config;

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

@Configuration
public class TocConfig {

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
