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
			var features = [ ],
				defaultStyleFunction = overlay.getStyleFunction ();
			
			overlay.getFeatures ().forEach (function (feature) {
				var serializedFeature = this._serializeFeature (feature, defaultStyleFunction);
				if (serializedFeature) {
					features.push (serializedFeature);
				}
			}, this);
			
			return {
				features: features
			};
		},
		
		_serializeFeature: function (/*ol.Feature*/feature, /*ol.style.StyleFunction*/defaultStyleFunction) {
			var format = new ol.format.GeoJSON (),
				overlay = feature.get ('_geoideOverlay'),
				styleFunction = feature.getStyleFunction () || defaultStyleFunction,
				resolution = this.get ('resolution');

			console.log ('Overlay style function: ', styleFunction);
			
			var styles = styleFunction (feature, resolution);
			
			console.log ('Feature styles: ', styles);
			
			if (!styles) {
				return null;
			}
			
			var serializedFeature = {
			};
			
			// Store the overlay:
			if (overlay) {
				serializedFeature.overlay = overlay.getProperties ();
			}
			
			// Store the styles and corresponding geometries:
			serializedFeature.styledGeometry = [ ];
			for (var i = 0; i < styles.length; ++ i) {
				var style = styles[i],
					geometry = style.getGeometryFunction () (feature);
				
				if (!geometry) {
					continue;
				}
				
				serializedFeature.styledGeometry.push ({
					geometry: format.writeGeometryObject (geometry),
					style: this._serializeStyle (style) 
				});
			}
			
			return serializedFeature;
		},
		
		_serializeStyle: function (style) {
			var fillStyle = style.getFill (),
				strokeStyle = style.getStroke (),
				imageStyle = style.getImage (),
				textStyle = style.getText (),
				zIndex = style.getZIndex () || 0;
			
			return {
				fill: this._serializeFillStyle (fillStyle),
				stroke: this._serializeStrokeStyle (strokeStyle),
				image: this._serializeImageStyle (imageStyle),
				text: this._serializeTextStyle (textStyle),
				zIndex: zIndex
			};
		},
		
		_serializeFillStyle: function (fillStyle) {
			if (!fillStyle) {
				return null;
			}
			
			return this._getObjectProperties (fillStyle, ['color']);
		},
		
		_serializeStrokeStyle: function (strokeStyle) {
			if (!strokeStyle) {
				return null;
			}
			
			return this._getObjectProperties (strokeStyle, ['color', 'lineCap', 'lineDash', 'lineJoin', 'miterLimit', 'width']);
		},
		
		_serializeImageStyle: function (imageStyle) {
			if (!imageStyle) {
				return null;
			}
			
			var result = this._getObjectProperties (imageStyle, ['opacity', 'rotateWithView', 'rotation', 'scale', 'snapToPixel', 'anchor', 'origin', 'size', 'fill', 'radius']);
			
			if (imageStyle.getFill) {
				result.type = 'circle';
				result.fill = this._serializeFillStyle (imageStyle.getFill ());
				result.stroke = this._serializeStrokeStyle (imageStyle.getStroke ());
			} else {
				result.type = 'image';
			}
			
			return result;
		},
		
		_serializeTextStyle: function (textStyle) {
			if (!textStyle) {
				return null;
			}
			
			var result = this._getObjectProperties (textStyle, ['font', 'offsetX', 'offsetY', 'rotation', 'scale', 'text', 'textAlign', 'textBaseline']);
			
			result.fill = this._serializeFillStyle (textStyle.getFill ());
			result.stroke = this._serializeStrokeStyle (textStyle.getStroke ());
			
			return result;
		},
		
		_getObjectProperties: function (/*Object*/object, /*String[]*/propertyNames) {
			var result = { };
			
			for (var i = 0; i < propertyNames.length; ++ i) {
				var name = propertyNames[i],
					getter = 'get' + name.substring (0, 1).toUpperCase () + name.substring (1);

				if (!object[getter]) {
					continue;
				}
				
				var value = object[getter] ();
				
				if (typeof value !== 'undefined') {
					result[name] = value;
				}
			}
			
			return result;
		}
	});
});