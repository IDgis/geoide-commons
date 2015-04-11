define ([
	'dojo/_base/declare',
	
	'dojo/Evented',
	
	'./Interaction',
	'dojo/has!config-OpenLayers-3?../engine/DrawGeometry-ol3:../engine/DrawGeometry-ol2'
], function (
	declare,
	Evented,
	Interaction,
	Engine
) {

	/**
	 * Configuration parameters:
	 * - type: Geometry type: point, line or polygon
	 * - format: format of the geometry passed to the event handlers. Accepted values: 'GeoJSON' or 'WKT'. 
	 * 
	 * Events:
	 * - drawstart: invoked when the user starts drawing geometry. The event object has no properties.
	 * - drawend: invoked when the user finishes drawing a geometry. The event object contains a single property 'geometry'
	 * containing the drawn geometry in the configured format.
	 */
	return declare ([Interaction, Evented, Engine], {
		type: 'point',
		format: 'geojson',
		modifier: 'none'
	});
});