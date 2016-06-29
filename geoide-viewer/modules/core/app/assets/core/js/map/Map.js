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
	
	var LayerRefList, LayerRef, LayerRefState, MapStatefulObject, MapStatefulArray;
	
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
					if (propertyName == 'layerRefs') {
						return new LayerRefList (value, self.map ());
					} else {
						return new MapStatefulArray (value, self.map ());
					}
				} else {
					return value;
				}
			} else {
				if (self.isInstanceOf (LayerRefList)) {
					return new LayerRef (lang.mixin ({
						layerRefs: [ ],
						state: { }
					}, value), self.map ());
				} else if (self.isInstanceOf (LayerRef) && propertyName == 'state') {
					return new LayerRefState (lang.mixin (lang.mixin ({}, defaultState), value), self.map ());				
					
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
	
	LayerRefList = declare ([MapStatefulArray], {
	});
	
	LayerRef = declare ([MapStatefulObject], {
	});
	
	LayerRefState = declare ([MapStatefulObject], {
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
			console.log(JSON.stringify(content));
			// Create layerRef index:
			var layerRefDictionary = { },
				layerRefList = [ ];
			
			var processLayerRefs = function (object) {
				var layerRefs = object.get ('layerRefs');
				if (!layerRefs) {
					return;
				}
				
				for (var i = 0, length = layerRefs.length (); i < length; ++ i) {
					var layerRef = layerRefs.get (i),
						id = layerRef.get ('id');
					if (!(id in layerRefDictionary)) {
						layerRefDictionary[id] = layerRef;
						layerRefList.push (layerRef);
					} else if (layerRefDictionary[id] !== layerRef) {
						throw new Error ('Duplicate layerRef with id: ', id);
					}
					
					processLayerRefs (layerRef);//, pid);
				}
			};
			
			processLayerRefs (this);
			
			this.set ('layerRefDictionary', layerRefDictionary);
			this.set ('layerRefList', layerRefList);
		},
		
		map: function () {
			return this;
		},
		
		getLayerRefById : function (layerRefId) {
			return this.get ('layerRefDictionary').get (layerRefId);
		},
		
		getInitialExtent: function () {
			return (this.get ('initial-extent'));
		}
		
		
		
	});
	
	
});