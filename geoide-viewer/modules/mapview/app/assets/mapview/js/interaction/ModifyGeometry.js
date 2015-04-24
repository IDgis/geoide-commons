define ([
	'dojo/_base/declare',
	'dojo/_base/array',
	'dojo/_base/lang',
	
	'dojo/Evented',
	
	'./Interaction',
	'./InteractionBase',
	
	'openlayers/ol'
], function (
	declare,
	array,
	lang,
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
		
		_currentEngine: null,
		_modifyInteraction: null,
		_selectInteraction: null,
		
		_selectedFeatures: null,
		_selectHandle: null,
		_overlayHandles: null,
		
		_enable: function (engine) {
			if (this._modifyInteraction) {
				this._disable ();
			}
			
			this._currentEngine = engine;
			
			this._selectedFeatures = [ ];
			
			var modifyConfig = {
			};
			
			if (this.source) {
				modifyConfig.source = this.source;
			} else if (this.features) {
				modifyConfig.features = this.features;
			}
			
			this._selectInteraction = new ol.interaction.Select ({
				condition: ol.events.condition.click
			});
			this._modifyInteraction = new ol.interaction.Modify ({
				deleteCondition: function (event) {
					return ol.events.condition.shiftKeyOnly (event) &&
						ol.events.condition.singleClick (event);
				},
				features: this._selectInteraction.getFeatures ()
			});

			engine.olMap.addInteraction (this._modifyInteraction);
			engine.olMap.addInteraction (this._selectInteraction);
			
			this._selectHandle = this._selectInteraction.on ('select', function (e) {
				array.forEach (e.deselected, this._unselectFeature, this);
				array.forEach (e.selected, this._selectFeature, this);
			}, this);
			
			// Register listeners on overlays:
			this._overlayHandles = [ ];
			this.features.forEach (function (feature) {
				var overlay = feature.get ('_geoideOverlay');
				
				if (overlay) {
					this._overlayHandles.push (overlay.on ('select', lang.hitch (this, function (e) {
						this.selectFeature (e.overlay.get ('feature'), e.keyEvent.shiftKey);
					})));
				}
			}, this);
		},
		
		_disable: function (engine) {
			if (!this._modifyInteraction) {
				return;
			}
			
			engine.olMap.removeInteraction (this._modifyInteraction);
			engine.olMap.removeInteraction (this._selectInteraction);
			
			// Unselect any selected features:
			array.forEach (this._selectedFeatures.concat ([]), this._unselectFeature, this);
			
			this._selectInteraction.unByKey (this._selectHandle);
			
			// Remove overlay watches:
			array.forEach (this._overlayHandles, function (h) { h.remove (); });
			
			this._currentEngine = null;
			this._selectedFeatures = null;
			this._selectHandle = null;
			this._modifyInteraction = null;
			this._selectInteraction = null;
			this._overlayHandles = null;
		},
		
		selectFeature: function (/*ol.Feature*/feature, /*Boolean*/addToSelection) {
			if (!this._selectedFeatures) {
				return;
			}
			
			for (var i = 0; i < this._selectedFeatures.length; ++ i) {
				if (feature === this._selectedFeatures[i]) {
					return;
				}
			}
			
			if (!addToSelection) {
				this._selectInteraction.getFeatures ().clear ();
				array.forEach (this._selectedFeatures.concat ([]), function (feature) {
					this._unselectFeature (feature);
				}, this);
			}
			
			this._selectInteraction.getFeatures ().push (feature);
			this._selectFeature (feature);
		},
		
		_selectFeature: function (/*ol.Feature*/feature) {
			// Add the feature to the list of selected features:
			this._selectedFeatures.push (feature);
			
			// Handle overlays:
			var overlay = feature.get ('_geoideOverlay');
			
			if (overlay) {
				overlay.set ('selected', true);
			}
		},
		
		_unselectFeature: function (/*ol.Feature*/feature) {
			// Remove the feature from the list of selected features:
			for (var i = 0; i < this._selectedFeatures.length; ++ i) {
				if (this._selectedFeatures[i] === feature) {
					this._selectedFeatures.splice (i, 1);
					break;
				}
			}

			// Handle overlays:
			var overlay = feature.get ('_geoideOverlay');
			
			if (overlay) {
				overlay.set ('selected', false);
			}
		},
		
		_removeFeature: function (/*ol.Feature*/feature) {
			var overlay = feature.get ('_geoideOverlay');
			
			if (overlay) {
				this._currentEngine.olMap.removeOverlay (overlay._overlay);
				overlay.remove ();
			}
			
			this.features.remove (feature);
		},
		
		deleteSelected: function () {
			if (!this._selectedFeatures || this._selectedFeatures.length === 0) {
				return;
			}
			
			this._selectInteraction.getFeatures ().clear ();
			
			var features = this._selectedFeatures.concat ([ ]);
			array.forEach (features, function (feature) {
				this._unselectFeature (feature);
				this._removeFeature (feature);
			}, this);
		}
	});
});