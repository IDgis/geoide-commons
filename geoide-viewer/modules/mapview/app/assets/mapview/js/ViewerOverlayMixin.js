define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/when',
	'openlayers/ol'
], function (
	declare,
	lang,
	when,
	ol
) {
	"use strict";
	
	return declare ([], {
		_overlays: null,
		
		/**
		 * Adds a vector overlay to the map viewer. If an overlay of the same name
		 * exists, that overlay is returned, otherwise a new overlay is created.
		 * 
		 * @param overlayName		The name of the overlay.
		 * @param overlayOptions	Creation options passed to the ol.FeatureOverlay constructor.
		 * @return					An instance ol.FeatureOverlay
		 */
		overlay: function (/*String*/overlayName, /*Object?*/overlayOptions) {
			overlayOptions = overlayOptions || { };
			
			// Look for an existing overlay:
			if (!this._overlays) {
				this._overlays = { };
			}
			
			if (overlayName in this._overlays) {
				var existingOverlay = this._overlays[overlayName];
				
				if ('style' in overlayOptions) {
					existingOverlay.setStyle (overlayOptions.style);
				}
				if ('features' in overlayOptions) {
					existingOverlay.setFeatures (overlayOptions.features);
				}
				
				return existingOverlay;
			}
			
			// Create a new feature overlay:
			var overlay = new ol.FeatureOverlay (overlayOptions || { });
			
			this._overlays[overlayName] = overlay;
				
			when (this.started, lang.hitch (this, function () {
				overlay.setMap (this.engine.olMap);
			}));
			
			return overlay;
		}
	});
});