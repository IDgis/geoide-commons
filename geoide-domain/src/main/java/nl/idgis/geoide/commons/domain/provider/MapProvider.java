package nl.idgis.geoide.commons.domain.provider;


import java.util.List;


import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.LayerRef;

public interface MapProvider {
	MapDefinition getMapDefinition (String mapId, String token);
	List<LayerRef> getLayers(String mapId, String token);
	List<LayerRef> getRootLayers(String mapId, String token);
	boolean reload();
	String getToken();
}
