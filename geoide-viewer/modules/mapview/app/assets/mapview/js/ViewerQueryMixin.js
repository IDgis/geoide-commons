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
		 * - layerRefs: layerRefs to include in the query, can be one of the following:
		 *   - Array of layerRef id's. Include each layer listed with the current layer state.
		 *   - Map with layerRef id's as key and objects as values. Includes each layerRef whose id
		 *     is a key in the map. Each object can have two keys:
		 *      query: the query to perform on this layerRef.
		 *      state: additional / custom layer state that is mixed in with the current layerRef state.
		 *      properties: list of properties to return
		 * - query: base query that is applied to each layerRef.
		 * - queryVisible: if set, only query visible layerRefs.
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
				xhr.post (geoideViewerRoutes.controllers.mapview.Query.query ().url, {
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
			
			var layerRefs = options.layerRefs || [ ],
				queryLayerRefs = [ ],
				queryVisible = ('queryVisible' in options) ? options.queryVisible : false,
				l;

			if (lang.isArray (layerRefs)) {
				for (var i = 0; i < layerRefs.length; ++ i) {
					l = map.getLayerRefById(layerRefs[i]);

					if (!l) {
						continue;
					}

					// Add a query layer:
					queryLayerRefs.push (this._createQueryLayerRef (map, l, options));
				}
			} else {
				for (var j in layerRefs) {
					if (!layerRefs.hasOwnProperty (j)) {
						continue;
					}
					
					l = map.getLayerRefById (layerRefs[j]);
					if (!l) {
						continue;
					}
					
					queryLayerRefs.push (this._createQueryLayerRef (map, l, layerRefs[j]));
				}
			}

			// Filter visible layers:
			if (queryVisible) {
				queryLayerRefs = array.filter (queryLayerRefs, function (l) {
					return !!l.state.visible;
				});
			}
			
			return {
				layers: queryLayerRefs,
				intersects: options.intersects || null,
				query: options.query || null
			};
		},
		
		_createQueryLayerRef: function (map, layerRef, properties) {

			properties = properties || { };

			
			//var layerState = this.layerState[layer.get ('id')] || { };
			var layerRefState = { };
			//tijdelijke hack om query over te zetten
			var queryState = this.getLayerRefState(layerRef.get ('id'), 'query');
			if(queryState) {
				layerRefState = { "query": queryState};
			}

			if (properties.state) {
				layerRefState = lang.mixin (lang.mixin ({ }, layerRefState), properties.state);
			}
			return {
				id: layerRef.get ('layerid'),
				query: properties.query || null,
				state: layerRefState
			};
		}
	});
});