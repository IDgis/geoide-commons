package nl.idgis.geoide.commons.domain.geometry;

import java.io.Serializable;

/**
 * Base interface for serializable OGC geometry.
 */
public interface Geometry extends Serializable {

	/**
	 * Returns the SRS of the geometry, or null if the geometry has no SRS.
	 * 
	 * @return The geometry SRS, or null if it has no SRS.
	 */
	Srs getSrs ();
	
	/**
	 * Returns the envelope of the geometry. A rectangular polygon.
	 * 
	 * @return The geometry's envelope.
	 */
	Geometry getEnvelope ();
	
	/**
	 * Returns the geometry type.
	 * 
	 * @return	The geometry type.
	 */
	GeometryType getType ();
	
	/**
	 * Returns the geometry as WKB.
	 * 
	 * @return The geometry as WKB.
	 */
	byte[] asBytes ();
	
	/**
	 * Returns the geometry as WKT.
	 * 
	 * @return The geometry as WKT.
	 */
	String asText ();
	
	/**
	 * Returns the "raw" envelope of the geometry as an {@link Envelope}, containing just the extents
	 * of the geometry.
	 * 
	 * @return The geometry envelope.
	 */
	Envelope getRawEnvelope ();
	
	/**
	 * Returns true if this geometry is of the given type, or is a subclass of the given type. Note that
	 * when this method returns true it means that the geometry can be represented as the given type, possibly
	 * by conversion. It does not mean that the "instanceof" operator should also return true for the same
	 * test.
	 * 
	 * @param type	The type to test for.
	 * @return		true if the geometry is of the given type, or is a subclass of the given type.
	 */
	boolean is (GeometryType type);
	
	/**
	 * Returns this geometry as an instance of the given type. Either by converting it to the given type,
	 * or by returning this geometry object if the conversion can be accomplished by a typecast.
	 *  
	 * @param type	The geometry type to convert to.
	 * @return		This geometry as an instance of the given type.
	 */
	<T extends Geometry> T as (GeometryType.Type<T> type);
	
	/**
	 * Returns this geometry as an instance of the given type. Either by converting it to the given type,
	 * or by returning this geometry object if the conversion can be accomplished by a typecast.
	 *  
	 * @param type	The geometry type to convert to.
	 * @return		This geometry as an instance of the given type.
	 */
	Geometry as (GeometryType type);
}