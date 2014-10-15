package geoide.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.idgis.geoide.commons.domain.traits.Trait;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.domain.traits.spring.TypedTrait;
import nl.idgis.geoide.commons.layer.DefaultLayerType;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LayerTypeConfig {

	@Configuration
	public static class LayerTypesConfig {
		@Bean
		@Autowired
		public DefaultLayerType defaultLayerType (final ServiceTypeRegistry serviceTypeRegistry) {
			return new DefaultLayerType (serviceTypeRegistry);
		}
	}
	
	@Bean
	@Autowired (required = false)
	public LayerTypeRegistry layerTypeRegistry (final Collection<LayerType> layerTypes, final @Qualifier ("layerTypeTrait") Collection<TypedTrait<?, ?>> traits) {
		final List<Traits<LayerType>> traitsLayerTypes = new ArrayList<> ();
		
		for (final LayerType lt: layerTypes) {
			traitsLayerTypes.add (TypedTrait.makeTraits (lt, traits));
		}
		
		return new LayerTypeRegistry (traitsLayerTypes);
	}
	
	public final static class A {
	}
	
	@Bean
	@Qualifier ("layerTypeTrait")
	public TypedTrait<A, A> dummyLayerTrait () {
		// Add a dymmy trait to make sure that the typedtraits collection always contains at least one trait.
		return TypedTrait.create (A.class, new Trait<A> () {
		});
	}
}
