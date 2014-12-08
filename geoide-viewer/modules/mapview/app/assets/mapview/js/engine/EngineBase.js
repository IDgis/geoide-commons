define ([
	'dojo/_base/declare',
	
	'../Stateful',
	
	'geoide-core/map/registry'
], function (
	declare,

	Stateful,
	
	registry
) {
	return declare ([Stateful], {
		viewer: null,
		layersList: null,
		layersMap: null,
		olMap: null,
		
		/**
		 * The policy to use for selecting a zoom level based on the scale: nearest, greater, smaller.
		 * A null value or a missing value indicates that no rounding to predefined levels is performed. 
		 */
		zoomPolicy: null,
		
		/**
		 * The units to use for the map: 'dd' (deegrees), 'm', 'ft', 'km', 'mi' or 'inches'.
		 */
		units: 'm',
		
		/**
		 * The number of units per pixel to assume for this view. Defaults to 0.00028, which corresponds
		 * to the OGC-default of 0.28mm per pixel assuming that a projection in meters is used for the map.
		 */
		unitsPerPixel: 0.00028,
		
		/**
		 * The minimal resolution.
		 */
		minResolution: 0.21,
		
		/**
		 * The maximum resolution.
		 */
		maxResolution: 3440.640,
		
		/**
		 * Array of valid resolutions for this map. If no list of resolutions is provided the map can always be freely scaled
		 * (the zoom policy has no effect).
		 */
		resolutions: null,
		
		constructor: function (viewer, initialState) {
			this.viewer = viewer;
			this.layersList = [ ];
			this.layersMap = { };
		},
		
		startup: function () {
			throw new Error ("Startup not implemented for this engine");
		},
		
		createLayer: function (/*Object*/serviceRequest) {
			return registry.layerType (serviceRequest.serviceIdentification.serviceType).create (serviceRequest);
		},
		
		updateLayer: function (/*Object*/existingOlLayer, /*Object*/serviceRequest) {
			registry.layerType (serviceRequest.serviceIdentification.serviceType).update (existingOlLayer, serviceRequest);
		},
		
		isLayerReusable: function (/*Object*/existingLayer, /*Object*/serviceRequest) {
			if (existingLayer.serviceType !== serviceRequest.serviceIdentification.serviceType) {
				return false;
			}
			
			return registry.layerType (existingLayer.serviceType).isReusable (existingLayer, serviceRequest);
		},
		
		setLayerVisibility: function (/*Object*/olLayer, /*Boolean*/visible) {
			throw new Error ("setLayerVisibility not implemented for this engine");
		},
		
		addLayer: function (/*Layer*/olLayer) {
			throw new Error ("addLayer not implemented for this engine");
		},
		
		setBaseLayer: function (/*Layer*/olLayer) {
		},
		
		setLayerIndex: function (/*Object*/olLayer, /*Number*/index) {
			throw new Error ("setLayerIndex not implemented for this engine");
		},
		
		removeLayer: function (/*Object*/olLayer) {
			throw new Error ("removeLayer not implemented for this engine");
		},
		
		setServiceRequests: function (serviceRequests) {
			var olLayers = [ ],
				newLayersList = [ ],
				newLayersMap = { };
			
			for (var i = 0; i < serviceRequests.length; ++ i) {
				var serviceRequest = serviceRequests[i],
					requestId = serviceRequest.id,
					layer = {
						id: serviceRequest.id,
						index: newLayersList.length,
						parameters: serviceRequest.parameters,
						serviceId: serviceRequest.serviceId,
						serviceType: serviceRequest.serviceIdentification.serviceType,
						removeCount: 0,
						olLayer: null
					};
				
				if (requestId in this.layersMap && this.isLayerReusable (this.layersMap[requestId], serviceRequest)) {
					// Re-use a previous layer with the same ID:
					var existingLayer = this.layersMap[requestId];
					layer.olLayer = existingLayer.olLayer;
					
					this.updateLayer (layer.olLayer, serviceRequest);
					
				} else {
					// Delete a previous layer with this ID:
					if (requestId in this.layersMap) {
						this.removeLayer (this.layersMap[requestId].olLayer);
						delete this.layersMap[requestId];
					}
					
					// Create a new layer and add it to the map:
					layer.olLayer = this.createLayer (serviceRequest);
					this.addLayer (layer.olLayer);
				}
				
				newLayersMap[layer.id] = layer;
				newLayersList.push (layer);
				
				// Move the layer to the correct index in the map and make it the base layer if it's the first layer:
				this.setLayerIndex (layer.olLayer, i);
				if (i === 0) {
					this.setBaseLayer (layer.olLayer);
				}
				this.setLayerVisibility (layer.olLayer, true);
			}
			
			// Hide or remove unused layers:
			for (var id in this.layersMap) {
				if (id in newLayersMap) {
					continue;
				}

				var removeableLayer = this.layersMap[id];

				if (++ removeableLayer.removeCount < 10) {
					// Keep the layer in an invisible state:
					this.setLayerVisibility (removeableLayer.olLayer, false);
					newLayersMap[id] = removeableLayer;
				} else {
					// The layer has been invisible for 10 updates, remove it:
					this.removeLayer (removeableLayer.olLayer);
				}
			}
			
			// Set the new layers list:
			this.layersMap = newLayersMap;
			this.layersList = newLayersList;
			
			return this;
		},
	
		/**
		 * Normalizes the given resolution value:
		 * - applies minResolution and maxResolution.
		 * - rounds to one of the fixed resolutions if this is dicated by the zoomPolicy
		 */
		_normalizeResolution: function (resolution) {
			if (resolution < this.get ('minResolution')) {
				resolution = this.get ('minResolution');
			}
			if (resolution > this.get ('maxResolution')) {
				resolution = this.get ('maxResolution');
			}
			
			var zoomPolicy = this.get ('zoomPolicy'),
				resolutions = this.get ('resolutions');
			
			if (typeof zoomPolicy === 'string' && resolutions) {
				var previous = -1,
					next = 9999;
				
				for (var i = 0; i < resolutions.length; ++ i) {
					if (Math.abs (resolutions[i] - resolution) < 0.0001) {
						// Exact match:
						previous = next = i;
						break;
					}
					
					if (resolutions[i] < resolution && i > previous) {
						previous = i;
					}
					if (resolutions[i] > resolution && i < next) {
						next = i;
					}
				}
				
				if (zoomPolicy == 'greater') {
					resolution = resolutions[next];
				} else if (zoomPolicy == 'smaller') {
					resolution = resolutions[previous];
				} else {
					resolution = resolution - resolutions[previous] < resolutions[next] - resolution ? resolutions[previous] : resolutions[next];
				}
			} 
			
			return resolution;
		},
		
		_scaleGetter: function () {
			return this.get ('resolution') / this.get ('unitsPerPixel');
		},
		
		_scaleSetter: function (value) {
			this.set ('resolution', value * this.get ('unitsPerPixel'));
		},
		
		_centerGetter: function () {
			throw new Error ('get center not implemented');
		},
		
		_centerSetter: function (value) {
			throw new Error ('set center not implemented');
		},
		
		_rotationGetter: function () {
			throw new Error ('get rotation not implemented');
		},
		
		_rotationSetter: function (value) {
			throw new Error ('set rotation not implemented');
		},
		
		_resolutionGetter: function () {
			throw new Error ('get resolution not implemented');
		},
		
		_resolutionSetter: function (resolution) {
			throw new Error ('set resolution not implemented');
		},
		
		_centerResolutionRotationGetter: function () {
			return {
				center: this.get ('center'),
				resolution: this.get ('resolution'),
				rotation: this.get ('rotation')
			};
		},
		
		_centerResolutionRotationSetter: function (value) {
			if (!value) {
				return;
			}
			if ('resolution' in value) {
				this.set ('resolution', value.resolution);
			}
			if ('center' in value) {
				this.set ('center', value.center);
			}
			if ('rotation' in value) {
				this.set ('rotation', value.rotation);
			}
		},
		
		_onMoveEnd: function () {
			this.viewer.emit ('moveEnd', { 
				viewer: this.viewer
			});
		},
		
		zoomTo: function (center, resolution, animate) {
			throw new Error ("zoomTo not implemented for this engine");
		},
		
		zoomToExtent: function (extent, animate) {
			throw new Error ("zoomToExtent not implemented for this engine");
		},
		
		updateSize: function () {
			throw new Error ("updateSize not implemented for this engine");
		},

		// =====================================================================
		// Interactions:
		// =====================================================================
		_interactionsGetter: function () {
			return this._interactions || [ ];
		},
		
		_interactionsSetter: function (interactions) {
			var currentInteractions = this.get ('interactions'),
				i;
			
			for (i = 0; i < currentInteractions.length; ++ i) {
				this.disableInteraction (currentInteractions[i]);
			}
			
			for (i = 0; i < interactions.length; ++ i) {
				this.enableInteraction (interactions[i]);
			}
		},
		
		enableInteraction: function (interaction) {
			if (!this._interactions) {
				this._interactions = [ ];
			}

			this._interactions.push (interaction);
			interaction._enable (this);
		},
		
		disableInteraction: function (interaction) {
			if (!this._interactions) {
				return;
			}
			
			for (var i = 0; i < this._interactions.length; ++ i) {
				if (this._interactions[i] !== interaction) {
					continue;
				}
				
				interaction._disable (this);
				this._interactions = this._interactions.splice (i, 1);
				break;
			}
		}
	});
});