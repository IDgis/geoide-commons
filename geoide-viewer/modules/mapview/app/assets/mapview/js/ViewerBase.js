define ([
    'dojo/_base/lang',
    'dojo/_base/array',
    'dojo/_base/declare',

    'dojo/dom-attr',
	'dojo/query',
	
	'dojo/when',
	'dojo/Deferred',
	
	'dojo/json',
	
	'dojo/Evented',

	'geoide-core/json-request',
	'geoide-core/map/registry',
	'geoide-core/DOMBehaviour',
	'geoide-core/map/MapBehaviour',
	'./Stateful',
	'./engine/Engine'
], function (
	lang,
	array,
	declare,
	
	domAttr,
	query,
	
	when,
	Deferred,

	json,
	
	Evented,

	jsonRequest,
	registry,
	DOMBehaviour,
	MapBehaviour,
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
	
	return declare ([Evented, Stateful, DOMBehaviour, MapBehaviour], {
		started: null,
		
		engine: null,
				
		layerRefState: null,
		
		activeLayerRef:null,
		
		_engineAttributes: null,
		_watchHandles: null,
		
		constructor: function (selector) {
			this.layerRefState = { };
			this._watchHandles = [ ];
			this.engine = new Engine (this, {
				center: [150000, 450000],
				zoom: 8
			});
			this.started = new Deferred ();

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
			var promise = this.inherited(arguments);
			var def = new Deferred ();
			
			promise.then ( lang.hitch (this, function(){
				if (this.started === true) {
					throw new Error ("Already started");
				}
				console.log("startup viewer");
				whenAll (this.map, this.engine.startup (), lang.hitch (this, function (map, engine) {
					console.log("startup viewer whenall" );
					console.log(map);
					this._watchLayerRefState ();
					
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
			}), function (err) {
				def.reject (err);
			});
			
			return def;
		},
		
		_watchLayerRefState: function () {
			var callback = lang.hitch (this, this._scheduleUpdate);
			
			this._watchHandles.push (this.map.get ('layerRefs').watchElements (callback));
			this.map.get ('layerRefList').forEach (lang.hitch (this, function (layerRef) {
				this._watchHandles.push (layerRef.get ('state').watch (callback));
				this._watchHandles.push (layerRef.get ('layerRefs').watchElements (callback));
			}));
		},
		
		setLayerRefState: function (layerRefId, key, value) {
			var deferred = new Deferred (),
				promises = [ ],
				doUpdate = false,
				values = { };
				
			if (typeof value === 'undefined') {
				values = key;
			} else {
				values[key] = value;
			}
			
			var setSingleValue = lang.hitch (this, function (key, value) {
				var deferred = new Deferred ();
				
				when (this._getLayerRefState (layerRefId, key), lang.hitch (this, function (previous) {
					if (value === previous) {
						deferred.resolve ();
						return;
					}
					
					doUpdate = true;
					
					when (this._setLayerRefState (layerRefId, key, value), function () {
						deferred.resolve ();
					});
				}));
				
				return deferred;
			});
			
			for (var i in values) {
				if (!values.hasOwnProperty (i)) {
					continue;
				}
				
				promises.push (setSingleValue (i, values[i]));
			}

			when (all (promises), lang.hitch (this, function (results) {
				if (doUpdate) {
					when (this._scheduleUpdate (), function () {
						deferred.resolve ();
					});
				} else {
					deferred.resolve ();
				}
			}));
			
			return deferred;
		},
		
		getLayerRefState: function (layerRefId, key) {
			return this._getLayerRefState (layerRefId, key);
		},
		
		getLayerProperty: function (layerRefId, key, defaultValue) {	
			return this._getLayerProperty (layerRefId, key, defaultValue);
		},
		
		
		/**
		 * Returns the current viewer state.
		 * 
		 * Options, a map containing any of the following properties:
		 * (none)
		 */
		getViewerState: function (/*Object?*/options) {
			return { 
				id: 'main', 
				mapid: this.mapId, 
				extent: this.getCurrentExtent (), 
				scale: this.get ('scale'), 
				resolution: this.get ('resolution'), 
				layerRefs: this._buildViewerState (this.map.get ('layerRefs')) 
			};
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
			console.log("_updateViewer");
			when (this.map, lang.hitch (this, function (map) {
				var viewerState = { layerRefs: this._buildViewerState (map.get ('layerRefs')) };
				
				// Post the viewer state:
				jsonRequest.post (geoideViewerRoutes.controllers.mapview.View.buildView ().url, {
					handleAs: 'json',
					headers: {
						'Content-Type': 'application/json'
					},
					data: json.stringify (viewerState)
				}).then (lang.hitch (this, function (data) {
					
					when (this.engine.setServiceRequests (data.serviceRequests), function () {
						def.resolve ();
					}, function (err) {
						def.reject (err);
					});
				}));
			}));
			
			return def;
		},
		
		_buildViewerState: function (layerRefs) {
			var result = [ ];
			
			for (var i = 0, length = layerRefs.length (); i < length; ++ i) {
				var layerRef = layerRefs.get (i),
					mergedLayerRef = { 
						layerid: layerRef.get('layerid'),
						state: layerRef.get ('state').extract ()
					};
				
				if (layerRef.get('layerRefs') !== undefined) {//layerRef.get ('layers').length () > 0) {
					mergedLayerRef.layerRefs = this._buildViewerState (layerRef.get ('layerRefs'));
				}
				
				result.push (mergedLayerRef);
			}
			
			return result;
		},

		_setLayerRefState: function (layerRefId, key, value) {
			var deferred = new Deferred ();
			
			when (this.map, function (map) {
				var layerRef = map.get ('layerRefDictionary').get (layerRefId);
				if (!layerRef) {
					throw new Error ("Unknown layerRef: " + layerRefId);
				}
				
				layerRef.get ('state').set (key, value);
				
				deferred.resolve ();
			});
			
			return deferred;
		},
		
		_getLayerRefState: function (layerRefId, key, defaultValue) {
			if (this.map.then) {
				var deferred = new Deferred ();
				
				when (this.map, function (map) {
					var layerRef = map.getLayerRefById (layerRefId);
					if (!layerRef) {
						throw new Error ("Unknown layer: " + layerRefId);
					}
					
					var value = layerRef.get ('state').get (key);
					
					deferred.resolve (typeof value === 'undefined' ? defaultValue : value);
				});
				
				return deferred;
			} else {
				var layerRef = this.map.getLayerRefById(layerRefId);
				if (!layerRef) {
					throw new Error ('Unknown layerRef: ' + layerRefId);
				}

				var value = layerRef.get ('state').get (key);
				
				return typeof value === 'undefined' ? defaultValue : value;
			}
		},
		_getLayerProperty: function (layerRefId, key, defaultValue) {
			if (this.map.then) {
				var deferred = new Deferred ();
				
				when (this.map, function (map) {
					var layerRef = map.getLayerRefById (layerRefId);
					if (!layerRef) {
						throw new Error ("Unknown layerRef: " + layerRefId);
					}
					if (layerRef.get ('properties')) {
						var value = layerRef.get ('properties').get (key);
						deferred.resolve (typeof value === 'undefined' ? defaultValue : value);
					}	
				});
				
				return deferred;
			} else {
				var layerRef = this.map.getLayerRefById (layerRefId);
				if (!layerRef) {
					throw new Error ('Unknown layerRef: '  + layerRefId);
				}
				var value;
				if (layerRef.get ('properties')) {
					value = layerRef.get ('properties').get (key);
				}	
				return typeof value === 'undefined' ? defaultValue : value;
			}
		},
		
		
		_parseConfig: function (config) {
			config = this.inherited (arguments);

			
			this._engineAttributes = [ ];
			
			for (var i in config) {
				this._setEngineAttribute (i, config[i]);
			}
			
			return { };
		},
		
		
		_activeLayerRefSetter: function (layerRefId) {
			if (layerRefId) {
				this.activeLayerRef = layerRefId;
			} else {
				this.activeLayerRef = null;
			}
		},
		_activeLayerRefGetter: function () {
			return this.activeLayerRef;
		},
		
		
		/**
		 * Interaction names:
		 * - navigation
		 */
		enableInteraction: function (interaction) {
			if (this.started === true) {
				this.engine.enableInteraction (interaction);
				return this;
			} else {
				var def = new Deferred ();
				when (this.started, lang.hitch (this, function () {
					this.engine.enableInteraction (interaction);
					def.resolve (this);
				}));
				return def;
			}
		},
		
			
		disableInteraction: function (interaction) {
			if (this.started === true) {
				this.engine.disableInteraction (interaction);
				return this;
			} else {
				var def = new Deferred ();
				when (this.started, lang.hitch (this, function () {
					this.engine.disableInteraction (interaction);
					def.resolve (this);
				}));
				return def;
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
		
		_unitsPerPixelGetter: function () {
			return this._getEngineAttribute ('unitsPerPixel');
		},
		
		_unitsPerPixelSetter: function (value) {
			return this._setEngineAttribute ('unitsPerPixel', value);
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
		
		_interactionsGetter: function () {
			return this._getEngineAttribute ('interactions');
		},
		
		_interactionsSetter: function (value) {
			return this._setEngineAttribute ('interactions', value);
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
				//console.log ('Storing engine attribute: ', name, value);
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
		},
		
		/**
		 * @param extent	The extent to zoom to: [minx, miny, maxx, maxy]
		 * @return			A scale
		 */
		getScaleForExtent: function (extent) {
			return this.engine.getScaleForExtent (extent);
		},
		
		/**
		 * @return			The current extent  [minx, miny, maxx, maxy]
		 */
		
		getCurrentExtent: function () {
			return { minx:this.engine.getCurrentExtent ()[0], miny: this.engine.getCurrentExtent ()[1], maxx: this.engine.getCurrentExtent ()[2], maxy: this.engine.getCurrentExtent ()[3]};
		},
		
		updateSize: function () {
			return this.engine.updateSize ();
		},
		
		
		report: function (templateInfo, /*Object?*/viewerStateOptions) {
			var def = new Deferred ();
			
			when (this.map, lang.hitch (this, function (map) {
				
				var viewerState = this.getViewerState(viewerStateOptions);
				
				var reportInfo = {viewerstates: [ viewerState ] , template: templateInfo};

				// Post the viewer state:
				jsonRequest.post (geoideReportRoutes.controllers.printservice.Report.report ().url, {
					handleAs: 'json',
					headers: {
						'Content-Type': 'application/json'
					},
					data: json.stringify (reportInfo)
				}).then (lang.hitch (this, function (data) {
					def.resolve (data);
				}), lang.hitch (this, function (error) {
					def.reject (error);
				}));
			}));
			
			return def;
		},
		
		templates: function () {
			var def = new Deferred ();

			jsonRequest.get (geoideReportRoutes.controllers.printservice.Template.getTemplates ().url).then (lang.hitch (this, function (data) {
					def.resolve (data);
			}));
			
			return def;

		}
	});
});
