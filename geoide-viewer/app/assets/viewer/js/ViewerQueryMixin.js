/* jshint -W099 */
define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/_base/array',
	
	'dojo/json',
	
	'dojo/request/xhr',
	
	'dojo/Deferred',
	'dojo/when'
], function (
	declare,
	lang,
	array,
	
	json,
	
	xhr,
	
	Deferred,
	when
) {
	return declare ([], {
		/**
		 * Performs a query for features on one or more layers in the map.
		 * 
		 * Options:
		 * - layers: layers to include in the query, can be one of the following:
		 *   - Array of layer id's. Include each layer listed with the current layer state.
		 *   - Map with layer id's as key and objects as values. Includes each layer whose id
		 *     is a key in the map. Each object can have two keys:
		 *      query: the query to perform on this layer.
		 *      state: additional / custom layer state that is mixed in with the current layer state.
		 *      properties: list of properties to return
		 * - query: base query that is applied to each layer.
		 * - queryVisible: if set, only query visible layers.
		 * 
		 * Returns:
		 * A deferred that resolves to an object containing the following keys:
		 * - features: an array of features
		 * - envelope: the envelope of the data. Only present if an envelope could be determined.
		 *   Empty feature collections or feature collections without geometry have no envelope.
		 * In case of a failure to perform the query on any of the requested layers, the promise
		 * is rejected with the error messages as cause.
		 *      
		 * Examples:
		 *  { layers: ['a, 'b', 'c', 'd'], query: { ... } }
		 *  
		 *  { 
		 *  	layers: {
		 *  		'a': {
		 *  			state: { ... },
		 *  			query: { ... },
		 *  			properties: [ ... ]
		 *  		},
		 *  		'b': {
		 *  			state: { ... },
		 *  			query: { ... },
		 *  			properties: [ ... ]
		 *  		}
		 *  	},
		 *  	query: { ... }
		 *  }
		 * 
		 */
		query: function (options) {
			var def = new Deferred ();

			when (this.map, lang.hitch (this, function (map) {
				// Build a query based on the current map and the given options:
				var q = this._buildQuery (map, options || { });
				
				// Execute the query:
				xhr.post (geoideViewerRoutes.controllers.viewer.Query.query ().url, {
					handleAs: 'json',
					headers: {
						'Content-Type': 'application/json'
					},
					data: json.stringify (q)
				}).then (function (data) {
					// Report failure:
					if (!data || !data.result || data.result !== 'ok') {
						def.reject (data.messages);
						return;
					}
					
					// Resolve the promise with the response:
					var result = { 
						features: data.features
					};
					
					if ('envelope' in data) {
						result.envelope = data.envelope;
					}
					
					def.resolve (result);
				}, function (error) {
					def.reject (error);
				});
			}));
			
			return def;
		},
		
		_buildQuery: function (/*Map*/map, /*Object*/options) {
			var layers = options.layers || [ ],
				queryLayers = [ ],
				queryVisible = ('queryVisible' in options) ? options.queryVisible : false,
				l;
			
			if (lang.isArray (layers)) {
				for (var i = 0; i < layers.length; ++ i) {
					l = map.getLayerById (layers[i]);
					if (!l) {
						continue;
					}

					// Add a query layer:
					queryLayers.push (this._createQueryLayer (map, l));
				}
			} else {
				for (var j in layers) {
					if (!layers.hasOwnProperty (j)) {
						continue;
					}
					
					l = map.getLayerById (layers[j]);
					if (!l) {
						continue;
					}
					
					queryLayers.push (this._createQueryLayers (map, l, layers[j]));
				}
			}

			// Filter visible layers:
			if (queryVisible) {
				queryLayers = array.filter (queryLayers, function (l) {
					return !!l.state.visible;
				});
			}
			
			return {
				layers: queryLayers,
				intersects: options.intersects || null
			};
		},
		
		_createQueryLayer: function (map, layer, properties) {
			properties = properties || { };
			
			var layerState = this.layerState[layer.id] || { };
			
			if (properties.state) {
				layerState = lang.mixin (lang.mixin ({ }, layerState), properties.state);
			}
			
			return {
				id: layer.id,
				query: properties.query || null,
				state: layerState
			};
		}
	});
});