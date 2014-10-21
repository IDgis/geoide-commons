package geoide.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.idgis.geoide.commons.domain.traits.Trait;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.domain.traits.spring.TypedTrait;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import nl.idgis.geoide.service.tms.TMSServiceType;
import nl.idgis.geoide.service.wfs.WFSServiceType;
import nl.idgis.geoide.service.wms.WMSServiceType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
	@Autowired (required = false)
	public ServiceTypeRegistry serviceTypeRegistry (final Collection<ServiceType> serviceTypes, final @Qualifier ("serviceTypeTrait") Collection<TypedTrait<?, ?>> traits) {
		final List<Traits<ServiceType>> traitsServiceTypes = new ArrayList<> ();
		
		for (final ServiceType st: serviceTypes) {
			traitsServiceTypes.add (TypedTrait.makeTraits (st, traits));
		}
		
		return new ServiceTypeRegistry (traitsServiceTypes);
	}
	
	public final static class A {
	}
	
	@Bean
	@Qualifier ("serviceTypeTrait")
	public TypedTrait<A, A> dummyServiceTrait () {
		// Add a dymmy trait to make sure that the typedtraits collection always contains at least one trait.
		return TypedTrait.create (A.class, new Trait<A> () {
		});
	}
}
