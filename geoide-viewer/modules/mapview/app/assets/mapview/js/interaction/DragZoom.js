define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	
	'./InteractionBase',
	
	'openlayers/ol'
], function (
	declare,
	lang,
	
	InteractionBase,
	
	ol
) {
		
	return declare ([InteractionBase], {
		_engine: null,
		
		_createInteractions: function (engine) {
			var boxInteraction = new ol.interaction.DragBox ({ 
				condition: ol.events.condition.always,
				style: new ol.style.Style ({
					stroke: new ol.style.Stroke ({
						color: [0, 0, 255, 1]
					})
				})
			});
			
			boxInteraction.on ('boxend', lang.hitch (this, function (e) {
				this._onBoxEnd (boxInteraction.getGeometry ().getExtent ());
			}));
			
			return [
				boxInteraction
			];
		},
		
		_enable: function (engine) {
			this.inherited (arguments);
			
			this._engine = engine;
		},
	
		_disable: function (engine) {
			this.inherited (arguments);
			
			this._engine = null;
		},
		
		_onBoxEnd: function (box) {
			if (!this._engine) {
				return;
			}
			
			this._engine.zoomToExtent (box, true);
		}
	});
});