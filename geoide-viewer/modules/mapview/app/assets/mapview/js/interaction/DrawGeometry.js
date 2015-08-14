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
	 * - format: format of the geometry passed to the event handlers. Accepted values: 'GeoJSON', 'WKT' or 'Raw'. 
	 * - source: (optional) an OpenLayers feature source to which features are drawn.
	 * - features: (optional) an OpenLayers feature collection to which features are drawn.
	 * 
	 * Events:
	 * - drawstart: invoked when the user starts drawing geometry. The event object has no properties.
	 * - drawend: invoked when the user finishes drawing a geometry. The event object contains a single property 'geometry'
	 * containing the drawn geometry in the configured format.
	 */
	return declare ([Interaction, Evented, InteractionBase], {
		type: 'point',
		format: 'raw',
		modifier: 'none',
		features: null,
		source: null,
		defaultStyle: null,
		
		_interaction: null,
		
		constructor: function (parameters) {
			if(parameters.style) {
				this.defaultStyle = parameters.style;
			} else {
				this.defaultStyle = new ol.style.Style({
					fill: new ol.style.Fill({color: [255,255,255,0.4]}),
					stroke: new ol.style.Stroke({color: '#ff4400', width: 2}),
					image: new ol.style.Circle({
						radius: 5,
						fill: new ol.style.Fill({
							color: [255,255,255,0.4]
						}),
						stroke: new ol.style.Stroke({
							color: '#ff4400',
							width: 2
						})
					})
				});
			}

		},
		
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
			case 'linestring':
				olType = 'LineString';
				break;
			}
			
			switch (this.format.toLowerCase ()) {
			default:
			case 'feature':
				olFormat = null;
				break;
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

			var drawConfig = {
				type: olType,
				condition: condition
			};
			
			if (this.source) {
				drawConfig.source = this.source;
			} else if (this.features) {
				drawConfig.features = this.features;
			} else {
				drawConfig.source = engine._vectorSource;
			}
			
			this._interaction = new ol.interaction.Draw (drawConfig);
				
			engine._vectorSource.clear ();
			
			this._interaction.on ('drawstart', function (e) {
				self.emit ('drawstart', { feature: e.feature });
			});
					
			this._interaction.on ('drawend', function (e) {
				e.feature.setStyle(self.defaultStyle);
				if (olFormat === null) {
					self.emit ('drawend', { geometry: e.feature.getGeometry (), feature: e.feature });
				} else {
					self.emit ('drawend', { geometry: olFormat.writeGeometry (e.feature.getGeometry ()), feature: e.feature });
				}
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