define ([
	'dojo/_base/declare',
	'./InteractionBase-ol3'
], function (
	declare,
	InteractionBase
) {
	
	return declare ([InteractionBase], {
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
			
			this._interaction = new ol.interaction.Draw ({
				source: engine._vectorSource,
				type: olType
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