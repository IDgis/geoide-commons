package nl.idgis.geoide.commons.domain.toc;

import java.util.List;

import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeTrait;

public interface TOCLayerTypeTrait extends LayerTypeTrait {
	public abstract List<Traits<TOCItem>> getTOC(LayerType type);
}
