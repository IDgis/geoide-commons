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
		},
		
		isReusable: function (existingLayer, serviceRequest) {
			if (existingLayer.serviceId != serviceRequest.serviceId) {
				return false;
			}
			
			return deepCompare (existingLayer.parameters, serviceRequest.parameters);
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
		mapNode: null,
		
		_vectorSource: null,
		_vectorLayer: null,
		
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
		
		setServiceRequests: function (serviceRequests) {
			this.olMap.removeLayer (this._vectorLayer);
			
			this.inherited (arguments);
			
			this.olMap.addLayer (this._vectorLayer);
		},
		
		setLayerVisibility: function (/*Object*/olLayer, /*Boolean*/visible) {
			olLayer.setVisible (visible);
		},
		
		addLayer: function (/*Layer*/olLayer) {
			this.olMap.addLayer (olLayer);
		},
		
		setLayerIndex: function (/*Object*/olLayer, /*Number*/index) {
			//throw new Error ("setLayerIndex not implemented for this engine");
		},
		
		removeLayer: function (/*Object*/olLayer) {
			this.olMap.removeLayer (olLayer);
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
		
		// =====================================================================
		// Utilities:
		// =====================================================================
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
		}
	});
});
