/* jshint -W099 */
define ([
    'dojo/_base/lang',
	'dojo/_base/declare',
	
	'dojo/Deferred',
	
	'./EngineBase',
	
	'../registry',
	
	'put-selector/put'
], function (
	lang,
	declare,
	
	Deferred,
	
	EngineBase,
	
	registry,
	
	put
) {

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
			console.log ('Creating TMS layer: ', serviceRequest.id);
			var serviceUrl = geoideViewerRoutes.controllers.mapview.Services.serviceRequestWithLayer (
					serviceRequest.serviceId,
					serviceRequest.parameters.layer,
					''
				).url;
			
			return new OpenLayers.Layer.TMS (
					serviceRequest.id, 
					serviceUrl, 
					{
						layername: '',
						type: 'png',
						serviceVersion: '',
						tileOrigin: new OpenLayers.LonLat (-285401.91999999998370, 22598.08000000000175),
						maxExtent: [-7000, 289000, 300000, 629000],
						displayOutsideMaxExtent: false,
						isBaseLayer: false,
						visibility: true,
						serverResolutions: [3440.640, 1720.320, 860.160, 430.080, 215.040, 107.520, 53.760, 26.880, 13.440, 6.720, 3.360, 1.680, 0.840, 0.420, 0.210], 
						getURL: function (bounds) {
							bounds = this.adjustBounds(bounds);
							var res = this.getServerResolution();
							var x = Math.round((bounds.left - this.tileOrigin.lon) / (res * this.tileSize.w));
							var y = Math.round((bounds.bottom - this.tileOrigin.lat) / (res * this.tileSize.h));
							var z = this.getServerZoom();
							var path = 'EPSG:28992' + '/' + z + "/" + x + "/" + y + "." + this.type;
							
							return geoideViewerRoutes.controllers.mapview.Services.serviceRequestWithLayer (
									serviceRequest.serviceId,
									serviceRequest.parameters.layer,
									path
								).url;
						}
					}
				);
		},
		
		update: function (existingOlLayer, serviceRequest) {
			console.log ('Updating TMS layer: ', serviceRequest.id);
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
			return new OpenLayers.Layer.WMS (
					serviceRequest.id,
					serviceUrl,
					serviceRequest.parameters,
					{
						isBaseLayer: false,
						singleTile: true,
						visibility: true
					}
				);
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

	function createLayer (serviceRequest) {
		return registry.layerType (serviceRequest.serviceIdentification.serviceType).create (serviceRequest);
	}
	
	function updateLayer (existingOlLayer, serviceRequest) {
		registry.layerType (serviceRequest.serviceIdentification.serviceType).update (existingOlLayer, serviceRequest);
	}
	
	function isLayerReusable (existingLayer, serviceRequest) {
		if (existingLayer.serviceType !== serviceRequest.serviceIdentification.serviceType) {
			return false;
		}
		
		return registry.layerType (existingLayer.serviceType).isReusable (existingLayer, serviceRequest);
	}
	
	return declare ([EngineBase], {
		mapNode: null,
		
		/**
		 * Startup should return "this", or a promise returning "this" after the
		 * map has started.
		 */
		startup: function () {
			var domNode = this.viewer.domNode;

			this.mapNode = put (domNode, 'div.geoide-map-ol2');
			this.olMap = new OpenLayers.Map (this.mapNode, {
				projection: 'EPSG:28992',
				maxExtent: new OpenLayers.Bounds (0, 289000, 300000, 620000),
				center: new OpenLayers.LonLat (150000, 450000),
				zoom: 8,
				tileSize: new OpenLayers.Size (256, 256),
				units: 'm',
				resolutions: this.get ('resolutions'),
				minResolution: this.get ('minResolution'),
				maxResolution: this.get ('maxResolution'),
				eventListeners: {
					moveend: lang.hitch (this, this._onMoveEnd)
				},
				fractionalZoom: true
			});
			
			// Fire the move end event during startup to match OpenLayers 3 behaviour:
			this._onMoveEnd ();
			
			return this;
		},
		
		setLayerVisibility: function (/*Object*/olLayer, /*Boolean*/visible) {
			olLayer.setVisibility (visible);
		},
		
		setBaseLayer: function (/*Object*/olLayer) {
			this.olMap.setBaseLayer (olLayer);
		},
		
		addLayer: function (/*Object*/olLayer) {
			this.olMap.addLayer (olLayer);
		},
		
		setLayerIndex: function (/*Object*/olLayer, /*Number*/index) {
			this.olMap.setLayerIndex (olLayer, index);
		},
		
		removeLayer: function (/*Object*/olLayer) {
			this.olMap.removeLayer (olLayer);
		},
		
		_centerGetter: function () {
			var center = this.olMap.getCenter ();
			return [center.lon, center.lat];
		},
		
		_centerSetter: function (xy) {
			this.olMap.setCenter (new OpenLayers.LonLat (xy[0], xy[1]));
		},
		
		_rotationGetter: function () {
			return 0;
		},
		
		_rotationSetter: function (value) {
		},
		
		_resolutionGetter: function () {
			return this.olMap.getResolution ();
		},
		
		_resolutionSetter: function (resolution) {
			this.olMap.setCenter (this.olMap.getCenter (), this.olMap.getZoomForResolution (this._normalizeResolution (resolution)));
		},
		
		zoomTo: function (center, resolution, animate) {
			var zoom = this.olMap.getZoomForResolution (this._normalizeResolution (resolution)),
				xy = new OpenLayers.LonLat (center[0], center[1]),
				def = new Deferred ();
			
			if (animate) {
				this.olMap.zoomTo (zoom, xy);
				setTimeout (function () {
					def.resolve ();
				}, 1000);
			} else {
				this.olMap.setCenter (xy, zoom);
				def.resolve ();
			}
			
			return def;
		},
		
		zoomToExtent: function (extent, animate) {
			var def = new Deferred (),
				zoom = this.olMap.getZoomForExtent (new OpenLayers.Bounds (extent[0], extent[1], extent[2], extent[3]), false),
				center = new OpenLayers.LonLat ((extent[0] + extent[2]) / 2.0, (extent[1] + extent[3]) / 2.0);
			
			if (animate) {
				this.olMap.zoomTo (zoom, extent);
				setTimeout (function () {
					def.resolve ();
				}, 1000);
			} else {
				this.olMap.setCenter (center, zoom);
				def.resolve ();
			}
			
			return def;
		}
	});
});