package nl.idgis.geoide.commons.domain.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;

public interface TableOfContents {
	CompletableFuture<List<Traits<TOCItem>>> getItems (MapDefinition mapDefinition);
}
