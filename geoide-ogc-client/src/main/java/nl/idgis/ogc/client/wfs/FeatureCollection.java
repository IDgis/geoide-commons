package nl.idgis.ogc.client.wfs;

/**
 * Base interface for feature collections. Must provide at least the contract of {@link Iterable} for {@link Feature}s.
 */
public interface FeatureCollection extends Iterable<Feature> {
}
