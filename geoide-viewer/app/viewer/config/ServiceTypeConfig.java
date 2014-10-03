package viewer.config;

import java.util.Collection;

import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.service.tms.TMSServiceType;
import nl.idgis.geoide.service.wms.WMSServiceType;
import nl.idgis.geoide.service.wfs.WFSServiceType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceTypeConfig {

	@Configuration
	public static class ServiceTypesConfig {
		@Bean
		public WMSServiceType wmsServiceType () {
			return new WMSServiceType ();
		}
		
		@Bean
		public TMSServiceType tmsServiceType () {
			return new TMSServiceType ();
		}
		
		@Bean
		public WFSServiceType wfsServiceType () {
			return new WFSServiceType ();
		}
	}
	
	@Bean
	@Autowired
	public ServiceTypeRegistry serviceTypeRegistry (final Collection<ServiceType> serviceTypes) {
		return new ServiceTypeRegistry (serviceTypes);
	}
}
