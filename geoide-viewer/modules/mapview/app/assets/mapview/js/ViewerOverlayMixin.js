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
	return declare ([], {
		_overlays: null,
		
		/**
		 * Returns the current viewer state. Extends the functionality of the base getViewerState with additional options.
		 * 
		 * Options, a map containing any of the following properties:
		 * - includeOverlays: true or false (default false). Whether to include overlays in the viewer state.
		 */
		getViewerState: function (/*Object?*/options) {
			options = options || { };

			var state = this.inherited (arguments);

			if (options.includeOverlays) {
				state.overlays = this._buildOverlayState ();
			}
			
			return state;
		},

		/**
		 * Extends the report function with the ability to include overlays in the print.
		 * 
		 * viewerStateOptions:
		 * - includeOverlays: true or false (default true). Whether to include overlays in the print.
		 */
		report: function (templateInfo, /*Object?*/viewerStateOptions) {
			viewerStateOptions = viewerStateOptions || { };
			
			return this.inherited (arguments, [templateInfo, lang.mixin ({ includeOverlays: true }, viewerStateOptions) ]);
		},

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
		},
		
		_buildOverlayState: function () {
			if (!this._overlays) {
				return { };
			}
			
			var overlays = { };
			
			for (var i in this._overlays) {
				if (!this._overlays.hasOwnProperty (i)) {
					continue;
				}
				
				overlays[i] = this._serializeOverlay (this._overlays[i]);
			}
			
			return overlays;
		},
		
		_serializeOverlay: function (/*ol.FeatureOverlay*/overlay) {
			var features = [ ];
			
			overlay.getFeatures ().forEach (function (feature) {
				features.push (this._serializeFeature (feature));
			}, this);
			
			return {
				features: features
			};
		},
		
		_serializeFeature: function (/*ol.Feature*/feature) {
			var format = new ol.format.GeoJSON (),
				overlay = feature.get ('_geoideOverlay');
			
			if (overlay) {
				return {
					type: 'Feature',
					geometry: format.writeGeometryObject (feature.getGeometry ()),
					properties: { 
						overlay: overlay.getProperties ()
					}
				};
			} else {
				return format.writeFeatureObject (feature);
			}
		}
	});
});