define ([
    'dojo/_base/lang',
    'dojo/_base/array',
    'dojo/_base/declare',

    'dojo/dom-attr',
	'dojo/query',
	
	'dojo/when',
	'dojo/Deferred',
	
	'dojo/request/xhr',

	'dojo/json',
	
	'dojo/Evented',
	
	'geoide-core/map/registry',
	'./LayerView',
	'./Stateful',
	'dojo/has!config-OpenLayers-3?./engine/engine-ol3:./engine/engine-ol2'
], function (
	lang,
	array,
	declare,
	
	domAttr,
	query,
	
	when,
	Deferred,

	xhr,
	
	json,
	
	Evented,
	
	registry,
	LayerView,
	Stateful,
	Engine
) {

	function wrapPromise (valueOrPromise) {
		if (valueOrPromise && typeof valueOrPromise.then === 'function') {
			return valueOrPromise;
		}
		
		var deferred = new Deferred ();
		deferred.resolve (valueOrPromise);
		return deferred;
	}
	
	var some = array.some;
	
	function all (objectOrArray) {
		var object, array;
		if(objectOrArray instanceof Array){
			array = objectOrArray;
		}else if(objectOrArray && typeof objectOrArray === "object"){
			object = objectOrArray;
		}

		var results;
		var keyLookup = [];
		if(object){
			array = [];
			for(var key in object){
				if(Object.hasOwnProperty.call(object, key)){
					keyLookup.push(key);
					array.push(object[key]);
				}
			}
			results = {};
		}else if(array){
			results = [];
		}

		if(!array || !array.length){
			return new Deferred().resolve(results);
		}

		var deferred = new Deferred();
		deferred.promise.always(function(){
			results = keyLookup = null;
		});
		var waiting = array.length;
		some(array, function(valueOrPromise, index){
			if(!object){
				keyLookup.push(index);
			}
			when(valueOrPromise, function(value){
				if(!deferred.isFulfilled()){
					results[keyLookup[index]] = value;
					if(--waiting === 0){
						deferred.resolve(results);
					}
				}
			}, deferred.reject);
			return deferred.isFulfilled();
		});
		return deferred.promise;	// dojo/promise/Promise
	}
	
	function whenAll () {
		var callback, errback;
		
		// Get the callback and optional errback:
		if (arguments.length < 2) {
			throw new Error ("Not enough arguments to whenAll");
		}
		if (typeof arguments[arguments.length - 2] === 'function') {
			callback = arguments[arguments.length - 2];
			errback = arguments[arguments.length - 1];
		} else {
			callback = arguments[arguments.length - 1];
			errback = null;
		}
		
		// Create a list of promises:
		var promiseList = [ ];
		for (var i = 0; i < arguments.length; ++ i) {
			promiseList[i] = wrapPromise (arguments[i]);
		}
		
		all (promiseList).then (function (results) {
			callback.apply (this, results);
		}, function (err) {
			if (errback) {
				errback (err);
			} 
		});
	}
	
	function pure (value) {
		var def = new Deferred ();
		def.resolve (value);
		return def;
	}
	
	return declare ([Evented, Stateful], {
		started: null,
		
		domNode: null,
		mapId: null,
		engine: null,
		
		map: null,
		
		layerState: null,
		
		_engineAttributes: null,
		_watchHandles: null,
		
		constructor: function (selector, config) {
			this.domNode = query (selector)[0];
			this.layerState = { };
			this._engineAttributes = [ ];
			this._watchHandles = [ ];
			this.engine = new Engine (this, {
				center: [150000, 450000],
				zoom: 8
			});
			this.started = new Deferred ();
			
			this._parse (config || { });
			console.log ('Creating viewer with map: ', this.mapId);
		},
	
		/**
		 * Initializes the viewer:
		 * - Fetches the map configuration from the server.
		 * - Starts the underlying engine.
		 * 
		 * Returns:
		 * A promise that is resolved when the viewer is fully started.
		 */
		startup: function () {
			if (this.started === true) {
				throw new Error ("Already started");
			}

			if (!this.map) {
				this.map = registry.map (this.mapId);
			}

			var def = new Deferred ();
			
			whenAll (this.map, this.engine.startup (), lang.hitch (this, function (map, engine) {
				this.map = map;
				
				this._watchLayerState ();
				
				// Update the viewer for the first time:
				this._updateViewer ().then (lang.hitch (this, function () {
					// Apply the engine attributes that have been set during startup:
					for (var i = 0; i < this._engineAttributes.length; ++ i) {
						var tuple = this._engineAttributes[i];
						this._setEngineAttribute (tuple[0], tuple[1], true);
					}
					this._engineAttributes = [ ];

					// Fire the "started" deferred and set the started flag to true:
					var startedDeferred = this.started;
					this.started = true;
					startedDeferred.resolve (true);
					
					// Fulfill the promise:
					def.resolve ();
				}));
			}), lang.hitch (this, function (err) {
				def.reject (err);
			}));
			
			return def;
		},
		
		_watchLayerState: function () {
			var callback = lang.hitch (this, this._scheduleUpdate);
			
			this._watchHandles.push (this.map.get ('layers').watchElements (callback));
			this.map.get ('layerList').forEach (lang.hitch (this, function (layer) {
				this._watchHandles.push (layer.get ('state').watch (callback));
				this._watchHandles.push (layer.get ('layers').watchElements (callback));
			}));
		},
		
		getLayerView: function (layerId) {
			return new LayerView (this, layerId);
		},
		
		setLayerState: function (layerId, key, value) {
			var previous;
			
			if (typeof value === 'undefined') {
				var doUpdate = false;
				
				for (var i in key) {
					if (!key.hasOwnProperty (i)) {
						continue;
					}
					
					var val = key[i];
					
					previous = this._getLayerState (layerId, i);
					
					if (val !== previous) {
						this._setLayerState (layerId, i, key[i]);
						doUpdate = true;
					}
				}
				
				if (doUpdate) {
					return this._scheduleUpdate ();
				}
			} else {
				previous = this._getLayerState (layerId, key);
				if (value !== previous) {
					this._setLayerState (layerId, key, value);
					return this._scheduleUpdate ();
				}
			}
			
			return pure (this);
		},
		
		getLayerState: function (layerId, key) {
			return this._getLayerState (layerId, key);
		},
		
		/**
		 * Schedules an update of the viewer at the next browser frame (setTimeout (..., 0)). The update
		 * is scheduled so that multiple changes to the viewer state are posted simultaneously.
		 * 
		 * Returns a deferred that fires when the update is complete.
		 */
		_scheduleUpdate: function () {
			if (this._updateScheduled) {
				return this._updateDeferred;
			} else if (this._updating) {
				if (!this._nextUpdateDeferred) {
					this._nextUpdateDeferred = new Deferred ();
				}
				
				return this._nextUpdateDeferred;
			}
			
			if (!this._updateDeferred) {
				this._updateDeferred = new Deferred ();
			}
			
			setTimeout (lang.hitch (this, function () {
				this._updateScheduled = false;
				this._updating = true;

				this._updateViewer ().then (lang.hitch (this, function () {
					var def = this._updateDeferred;
					
					this._updating = false;
					
					if (this._nextUpdateDeferred) {
						this._updateDeferred = this._nextUpdateDeferred;
						this._nextUpdateDeferred = null;
						this._scheduleUpdate ();
					} else {
						this._updateDeferred = null;
					}
					
					
					def.resolve (this);
				}), lang.hitch (this, function (err) {
					var def = this._updateDeferred;
					
					this._updating = false;
					
					if (this._nextUpdateDeferred) {
						this._updateDeferred = this._nextUpdateDeferred;
						this._nextUpdateDeferred = null;
						this._scheduleUpdate ();
					} else {
						this._updateDeferred = null;
					}
					
					
					def.reject (err);
				}));
			}), 0);
			
			this._updateScheduled = true;
			return this._updateDeferred;
		},
		
		/**
		 * Posts the current map and map state to the server, requesting a new map viewer configuration
		 * that takes into account the new state.
		 * 
		 * Returns a promise that is resolved when the current viewer state is in effect.
		 */
		_updateViewer: function () {
			var def = new Deferred ();
			
			when (this.map, lang.hitch (this, function (map) {
				var viewerState = { layers: this._buildViewerState (map.get ('layers')) };
				
				// Post the viewer state:
				xhr.post (geoideViewerRoutes.controllers.mapview.View.buildView ().url, {
					handleAs: 'json',
					headers: {
						'Content-Type': 'application/json'
					},
					data: json.stringify (viewerState)
				}).then (lang.hitch (this, function (data) {
					console.log ('Viewer state: ', data);
					
					when (this.engine.setServiceRequests (data.serviceRequests), function () {
						def.resolve ();
					}, function (err) {
						def.reject (err);
					});
				}));
			}));
			
			return def;
		},
		
		_buildViewerState: function (layers) {
			var result = [ ];
			
			for (var i = 0, length = layers.length (); i < length; ++ i) {
				var layer = layers.get (i),
					mergedLayer = { 
						id: layer.get ('id'),
						state: layer.get ('state').extract ()
					};
				
				if (layer.get ('layers').length () > 0) {
					mergedLayer.layers = this._buildViewerState (layer.get ('layers'));
				}
				
				result.push (mergedLayer);
			}
			
			return result;
		},

		_setLayerState: function (layerId, key, value) {
			if (!(layerId in this.layerState)) {
				this.layerState[layerId] = { };
			}
			
			this.layerState[layerId][key] = value;
		},
		
		_getLayerState: function (layerId, key, defaultValue) {
			if (!(layerId in this.layerState) || !(key in this.layerState[layerId])) {
				return defaultValue;
			}
			
			return this.layerState[layerId][key];
		},
		
		_parse: function (config) {
			this.map = config.map;
			this.mapId = config.mapId || domAttr.get (this.domNode, 'data-geoide-map');
			
			for (var i in config) {
				if (i == 'mapId') {
					continue;
				}
				
				this._setEngineAttribute (i, config[i]);
			}
		},
		
		// =====================================================================
		// Attributes:
		// =====================================================================
		_zoomGetter: function () {
			return this._getEngineAttribute ('zoom');
		},
		
		_zoomSetter: function (value) {
			return this._setEngineAttribute ('zoom', value);
		},
		
		_scaleGetter: function () {
			return this._getEngineAttribute ('scale');
		},
		
		_scaleSetter: function (value) {
			return this._setEngineAttribute ('scale', value);
		},
		
		_centerGetter: function () {
			return this._getEngineAttribute ('center');
		},
		
		_centerSetter: function (value) {
			return this._setEngineAttribute ('center', value);
		},
		
		_rotationGetter: function () {
			return this._getEngineAttribute ('rotation');
		},
		
		_rotationSetter: function (value) {
			return this._setEngineAttribute ('rotation', value);
		},
		
		_resolutionGetter: function () {
			return this._getEngineAttribute ('resolution');
		},
		
		_resolutionSetter: function (value) {
			return this._setEngineAttribute ('resolution', value);
		},
		
		_centerResolutionRotationGetter: function () {
			return this._getEngineAttribute ('centerResolutionRotation');
		},
		
		_centerResolutionRotationSetter: function (value) {
			return this._setEngineAttribute ('centerResolutionRotation', value);
		},
		
		_getEngineAttribute: function (name) {
			if (this.started === true) {
				return this.engine.get (name);
			} else {
				var def = new Deferred ();
				when (this.started, lang.hitch (this, function () {
					def.resolve (this.engine.get (name));
				}));
				return def;
			}
		},
		
		_setEngineAttribute: function (name, value, force) {
			if (this.started === true || force) {
				this.engine.set (name, value);
				return this;
			} else {
				console.log ('Storing engine attribute: ', name, value);
				this._engineAttributes.push ([ name, value ]);
				var def = new Deferred ();
				when (this.started, lang.hitch (this, function () {
					def.resolve (this);
				}));
				return def;
			}
		},
		
		// =====================================================================
		// Utility functions:
		// =====================================================================
		/**
		 * @param center		The new center: [x, y]
		 * @param resolution	The new map resolution
		 * @param animate		When true, use an animation to set the new map extent.
		 * @return				A promise that is resolved when the animation completes.
		 */
		zoomTo: function (center, resolution, animate) {
			return this.engine.zoomTo (center, resolution, animate);
		},
		
		/**
		 * @param extent	The extent to zoom to: [minx, miny, maxx, maxy]
		 * @param animate	When true, use an animation to set the new map extent.
		 * @return			A promise that is resolved when the animation completes.
		 */
		zoomToExtent: function (extent, animate) {
			return this.engine.zoomToExtent (extent, animate);
		}
	});
});