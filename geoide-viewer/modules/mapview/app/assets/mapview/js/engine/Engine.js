define ([
	'dojo/_base/declare',
	
	'dojo/Deferred',
	
	'./EngineBase',
	
	'geoide-core/map/registry',
	
	'put-selector/put'
], function (
	declare,
	
	Deferred,
	
	EngineBase,
	
	registry,
	
	put
) {
	
	var projection = new ol.proj.Projection ({ code: 'EPSG:28992', units: 'm' }),
		extent = [-7000, 289000, 300000, 629000],
		origin = [-285401.91999999998370, 22598.08000000000175],
		resolutions = [3440.640, 1720.320, 860.160, 430.080, 215.040, 107.520, 53.760, 26.880, 13.440, 6.720, 3.360, 1.680, 0.840, 0.420, 0.210];

	function elastic(t) {
		return Math.pow(2, -10 * t) * Math.sin((t - 0.075) * (2 * Math.PI) / 0.3) + 1;
	}
	
	function tileUrlFunction (urlBase, tileCoord, pixelRatio, projection) {
		return urlBase + tileCoord.a.toString () + '/' + tileCoord.x.toString () + '/' + tileCoord.y.toString () + '.png';
	}
	
	function deepCompare (a, b) {
		for (var i in a) {
			if (!(i in b)) {
				return false;
			}
			
			if (a[i] !== b[i]) {
				return false;
			}
		}
		
		for (var j in b) {
			if (!(j in a)) {
				return false;
			}
		}
		
		return true;
	}
	
	// TMS layer type:
	registry.registerLayerType ('TMS', {
		create: function (serviceRequest) {
			var urlBase = serviceRequest.serviceIdentification.serviceEndpoint + '/' + serviceRequest.parameters.layer + '/';
			
			return new ol.layer.Tile ({
				source: new ol.source.TileImage ({
					crossOrigin: null,
					extent: extent,
					tileGrid: new ol.tilegrid.TileGrid ({
						origin: origin,
						resolutions: resolutions
					}),
					tileUrlFunction: function (tileCoord, pixelRatio, projection) {
						var path = projection.getCode () + '/' + tileCoord[0] + '/' + tileCoord[1] + '/' + tileCoord[2] + '.png';
						return geoideViewerRoutes.controllers.mapview.Services.serviceRequestWithLayer (
							serviceRequest.serviceId,
							serviceRequest.parameters.layer,
							path
						).url;
					}
				})
			});
		},
		
		update: function (existingOlLayer, serviceRequest) {
		},
		
		isReusable: function (existingLayer, serviceRequest) {
			if (existingLayer.serviceId != serviceRequest.serviceId) {
				return false;
			}
			
			return deepCompare (existingLayer.parameters, serviceRequest.parameters);
		}
	});
	
	// WMS layer type:
	registry.registerLayerType ('WMS', {
		create: function (serviceRequest) {
			var serviceUrl = serviceRequest.serviceIdentification.serviceEndpoint;
			return new ol.layer.Image ({
				source: new ol.source.ImageWMS ({
					crossOrigin: null,
					params: serviceRequest.parameters,
					resolutions: resolutions,
					url: serviceUrl 
				})
			});
		},
		
		update: function (existingOlLayer, serviceRequest) {
			existingOlLayer.getSource ().updateParams (serviceRequest.parameters);
		},
		
		isReusable: function (existingLayer, serviceRequest) {
			// Service ID's need to be equal:
			if (existingLayer.serviceId != serviceRequest.serviceId) {
				return false;
			}
			
			// The existing layer must have an OpenLayers layer:
			if (!existingLayer.olLayer) {
				return false;
			}
			
			// The service endpoint must be equal:
			if (serviceRequest.serviceIdentification.serviceEndpoint != existingLayer.olLayer.getSource ().getUrl ()) {
				return false;
			}
			
			return true;
		}
	});

	var interactions = {
		'navigation': [ 
			new ol.interaction.DragRotate (), 
			new ol.interaction.DoubleClickZoom (), 
			new ol.interaction.DragPan ({
				kinetic: new ol.Kinetic (-0.005, 0.05, 100)
			}), 
			new ol.interaction.PinchRotate (),
			new ol.interaction.PinchZoom (),
			new ol.interaction.MouseWheelZoom (),
			new ol.interaction.DragZoom ()
		],
		'keyboardNavigation': [
			new ol.interaction.KeyboardPan (),
			new ol.interaction.KeyboardZoom ()
		],
		'dragZoom': [
			new ol.interaction.DragZoom ({ condition: ol.events.condition.always })
			
		],
		'drawGeometry': [
			function (config, self) {
				var type = config.type || 'point',
					format = config.format || 'geojson',
					olType,
					olFormat;
				switch (('' + type).toLowerCase ()) {
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
				
				switch (('' + format).toLowerCase ()) {
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
				
				var drawInteraction = new ol.interaction.Draw ({
					source: self._vectorSource,
					type: olType
				});

				drawInteraction.on ('drawstart', function (e) {
					// Clear the vector source:
					self._vectorSource.clear ();
				});
				
				drawInteraction.on ('drawend', function (e) {

					// Raise the draw event:
					
				});
				
				
				return drawInteraction;
			}
		]
	};
	
	return declare ([EngineBase], {
		viewer: null,
		layersList: null,
		layersMap: null,
		olMap: null,
		
		mapNode: null,
		
		_vectorSource: null,
		_vectorLayer: null,
		
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
			var domNode = this.viewer.node;

			this.mapNode = put (domNode, 'div.geoide-map-ol3');
			
			this._vectorSource = new ol.source.Vector ();
			this._vectorLayer = new ol.layer.Vector ({
				source: this._vectorSource,
				style: new ol.style.Style ({
					fill: new ol.style.Fill ({
						color: 'rgba(255, 255, 255, 0.2)'
					}),
					stroke: new ol.style.Stroke ({
						color: '#ffcc33',
						width: 2
					}),
					image: new ol.style.Circle ({
						radius: 7,
						fill: new ol.style.Fill ({
							color: '#ffcc33'
						})
					})
				})
			});
			
			this.olMap = new ol.Map ({
				layers: [ ],
				renderer: 'canvas',
				target: this.mapNode,
				view: new ol.View ({
					center: [150000, 450000],
					zoom: 8,
					projection: projection,
					minResolution: this.get ('minResolution'),
					maxResolution: this.get ('maxResolution')
				}),
				interactions: [ ]	// Map defaults to no interactions.
			});
			
			this.olMap.on ('moveend', this._onMoveEnd, this);
			
			return this;
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
		
		setServiceRequests: function (serviceRequests) {
			this.olMap.removeLayer (this._vectorLayer);
			
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
			
			this.olMap.addLayer (this._vectorLayer);
			
			return this;
		},
		
		setLayerVisibility: function (/*Object*/olLayer, /*Boolean*/visible) {
			olLayer.setVisible (visible);
		},
		
		addLayer: function (/*Layer*/olLayer) {
			this.olMap.addLayer (olLayer);
		},
		
		setLayerIndex: function (/*Object*/olLayer, /*Number*/index) {
			var layers = this.olMap.getLayers ();
			
			layers.remove (olLayer);
			layers.insertAt (index, olLayer);
		},
		
		removeLayer: function (/*Object*/olLayer) {
			this.olMap.removeLayer (olLayer);
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
			var center = this.olMap.getView ().getCenter ();
			
			// Make a copy to avoid leaking OpenLayers 3 internals to the outside world:
			return [ center[0], center[1] ];
		},
		
		_centerSetter: function (center) {
			this.olMap.getView ().setCenter (center);
		},
		
		_rotationGetter: function () {
			return this.olMap.getView ().getRotation ();
		},
		
		_rotationSetter: function (rotation) {
			this.olMap.getView ().setRotation (rotation);
		},
		
		_resolutionGetter: function () {
			return this.olMap.getView ().getResolution ();
		},
		
		_resolutionSetter: function (resolution) {
			this.olMap.getView ().setResolution (this._normalizeResolution (resolution));
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
		
		// =====================================================================
		// Utilities:
		// =====================================================================
		_onMoveEnd: function () {
			this.viewer.emit ('moveEnd', { 
				viewer: this.viewer
			});
		},
		
		zoomTo: function (center, resolution, animate) {
			var def = new Deferred ();
			
			if (animate) {
				var view = this.olMap.getView (),
					pan = ol.animation.pan ({
						duration: 1000,
						source: this.olMap.getView ().getCenter ()
					}),
					zoom = ol.animation.zoom ({
						duration: 1000,
						resolution: this.olMap.getView ().getResolution ()
					});
				
				this.olMap.beforeRender (pan, zoom);
				view.setResolution (resolution);
				view.setCenter (center);
				
				setTimeout (function () {
					def.resolve ();
				}, 1000);
			} else {
				def.resolve ();
				this.set ('resolution', resolution);
				this.set ('center', center);
			}
			
			return def;
		},
		
		zoomToExtent: function (extent, animate) {
			var def = new Deferred (),
				view = this.olMap.getView ();
			
			if (animate) {
				var pan = ol.animation.pan ({
						duration: 1000,
						source: this.olMap.getView ().getCenter ()
					}),
					zoom = ol.animation.zoom ({
						duration: 1000,
						resolution: this.olMap.getView ().getResolution ()
					});
				
				this.olMap.beforeRender (pan, zoom);
				
				setTimeout (function () {
					def.resolve ();
				}, 1000);
			} else {
				def.resolve ();
			}
			
			view.fitExtent (extent, this.olMap.getSize ());
			
			return def;
		},
		
		getScaleForExtent: function (extent) {
			var resolution = this.olMap.getView ().getResolutionForExtent (extent, this.olMap.getSize ());
			var scale = resolution / this.get ('unitsPerPixel');
			return scale;
		},
		
		getCurrentExtent: function () {
			return this.olMap.getView ().calculateExtent(this.olMap.getSize());
		},
		
		updateSize: function () {
			this.olMap.updateSize();
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
				this._interactions.splice (i, 1);
				break;
			}
		}
	});
});
