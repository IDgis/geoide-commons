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
	"use strict";
	
	/**
	 * Configuration parameters:
	 * - source: (optional) an OpenLayers feature source from which features are modified.
	 * - features: (optional) an OpenLayers feature collection from which features are modified.
	 * 
	 * Either "source" or "features" must be provided.
	 */
	return declare ([Interaction, Evented, InteractionBase], {
		features: null,
		source: null,
		
		_interaction: null,
		
		_enable: function (engine) {
			if (this._interaction) {
				this._disable ();
			}
			
			var modifyConfig = {
				deleteCondition: function (event) {
					return ol.events.condition.shiftKeyOnly (event) &&
						ol.events.condition.singleClick (event);
				}
			};
			
			if (this.source) {
				modifyConfig.source = this.source;
			} else if (this.features) {
				modifyConfig.features = this.features;
			}
			
			this._interaction = new ol.interaction.Modify (modifyConfig);
			
			engine.olMap.addInteraction (this._interaction);
		},
		
		_disable: function (engine) {
			if (!this._interaction) {
				return;
			}
			
			engine.olMap.removeInteraction (this._interaction);
			this._interaction = null;
		}
	});
});