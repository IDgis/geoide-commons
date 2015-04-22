define ([
	'dojo/_base/declare',
	
	'dojo/Evented',
	
	'./Interaction',
	'./InteractionBase',
	
	'openlayers/ol'
], function (
	declare,
	Evented,
	Interaction,
	InteractionBase,
	
	ol
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
	return declare ([Interaction, Evented, InteractionBase], {
		type: 'point',
		format: 'geojson',
		modifier: 'none',
		
		_interaction: null,
		
		_enable: function (engine) {
			if (this._interaction) {
				this._disable ();
			}
			
			var olType,
				olFormat,
				self = this;
			
			switch (this.type.toLowerCase ()) {
			default:
			case 'point':
				olType = 'Point';
				break;
			case 'polygon':
				olType = 'Polygon';
				break;
			case 'lineString':
				olType = 'LineString';
				break;
			}
			
			switch (this.format.toLowerCase ()) {
			default:
			case 'geojson':
				olFormat = new ol.format.GeoJSON ();
				break;
			case 'wkt':
				olFormat = new ol.format.WKT ();
				break;
			case 'gml':
				olFormat = new ol.format.GML ();
				break;
			}
			
			switch (this.modifier.toLowerCase ()) {
				default:
				case 'shiftKey':
					condition = ol.events.condition.shiftKeyOnly;
					break;
				case 'none':
					condition = ol.events.condition.noModifierKeys;
					break;
			}
			
			
			this._interaction = new ol.interaction.Draw ({
				source: engine._vectorSource,
				type: olType,
				condition: condition
			});
			
			engine._vectorSource.clear ();
	
			this._interaction.on ('drawstart', function (e) {
				self.emit ('drawstart', { });
			});
			
			this._interaction.on ('drawend', function (e) {
				self.emit ('drawend', { geometry: olFormat.writeGeometry (e.feature.getGeometry ()) });
			});

			engine.olMap.addInteraction (this._interaction);
		},
		
		_disable: function (engine) {
			if (!this._interaction) {
				return;
			}
			
			engine._vectorSource.clear ();
			
			engine.olMap.removeInteraction (this._interaction);
			this._interaction = null;
		}
	});
});