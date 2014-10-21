define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	
	'../StatefulBase',
	'../StatefulObject',
	'../StatefulArray'
], function (
	declare,
	lang,
	StatefulBase,
	StatefulObject,
	StatefulArray
) {
	
	var LayerList, Layer, LayerState, MapStatefulObject, MapStatefulArray;
	
	var defaultState = {
		visible: true
	};
	
	var mapWrap = function (value, self, propertyName) {
		if (typeof value == 'object') {
			if (value.isInstanceOf && value.isInstanceOf (StatefulBase)) {
				return value;
			} else if ('length' in value) {
				// Wrap empty arrays or arrays containing objects:
				if (value.length === 0 || typeof value[0] == 'object') {
					if (propertyName == 'layers') {
						return new LayerList (value, self.map ());
					} else {
						return new MapStatefulArray (value, self.map ());
					}
				} else {
					return value;
				}
			} else {
				if (self.isInstanceOf (LayerList)) {
					return new Layer (lang.mixin ({
						layers: [ ],
						state: { }
					}, value), self.map ());
				} else if (self.isInstanceOf (Layer) && propertyName == 'state') {
					return new LayerState (lang.mixin (lang.mixin ({}, defaultState), value), self.map ());				
					
				} else {
					return new MapStatefulObject (value, self.map ());
				}
			}
		}
		
		return value;
	};	

	var MapStatefulBase = declare ([], {
		_map: null,
		_wrap: mapWrap,
		
		constructor: function (value, map) {
			this._map = map;
		},
		
		map: function () {
			return this._map;
		}
	});
	
	MapStatefulObject = declare ([StatefulObject, MapStatefulBase], {
		constructor: function (content) {
			this._buildContent (content);
		}
	});
	
	MapStatefulArray = declare ([StatefulArray, MapStatefulBase], {
		constructor: function (content) {
			this._buildContent (content);
		}
	});
	
	LayerList = declare ([MapStatefulArray], {
	});
	
	Layer = declare ([MapStatefulObject], {
	});
	
	LayerState = declare ([MapStatefulObject], {
	});
	
	return declare ([StatefulObject], {
		_wrap: mapWrap,
		_schema: {
			layerDictionary: {
				'transient': true
			},
			layerList: {
				'transient': true
			}
		},
		
		constructor: function (content) {
			this._buildContent (content);
			
			// Create layer index:
			var layerDictionary = { },
				layerList = [ ];
			
			var processLayers = function (object) {
				var layers = object.get ('layers');
				if (!layers) {
					return;
				}
				
				for (var i = 0, length = layers.length (); i < length; ++ i) {
					var layer = layers.get (i),
						id = layer.get ('id');
					
					if (!(id in layerDictionary)) {
						layerDictionary[id] = layer;
						layerList.push (layer);
					} else if (layerDictionary[id] !== layer) {
						throw new Error ('Duplicate layer with id: ', id);
					}
					
					processLayers (layer);
				}
			};
			
			processLayers (this);
			
			this.set ('layerDictionary', layerDictionary);
			this.set ('layerList', layerList);
		},
		
		map: function () {
			return this;
		}
	});
});