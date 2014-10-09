define ([
	'dojo/_base/lang',
	'dojo/_base/declare',
	
	'dojo/Evented'
], function (
	lang,
	declare,
	
	Evented
) {

	return declare ([Evented], {
		structure: null,
		layersMap: null,
		layersList: null,
		
		constructor: function (structure) {
			this.structure = structure;
			
			// Create a layer index:
			this.layersMap = { };
			this.layersList = [ ];
			this._createLayerIndex (this.structure);
		},
		
		getId: function () {
			return this.structure.id;
		},
		
		hasLayer: function (layerId) {
			return layerId in this.layersMap;
		},
		
		getLayers: function () {
			return this.layersList;
		},
		
		getRootLayers: function () {
			return this.structure.layers || [ ]; 
		},
		
		getLayerById: function (layerId) {
			if (!this.hasLayer (layerId)) {
				return null;
			}
			
			return this.layersMap[layerId];
		},
		
		_createLayerIndex: function (structure) {
			if (!structure.layers) {
				return;
			}
			
			for (var i = 0; i < structure.layers.length; ++ i) {
				var layer = structure.layers[i],
					id = layer.id;
				
				if (!id) {
					throw new Error ("Map " + this.structure.id + " has a layer without an ID");
				}
				if (this.layersMap[id]) {
					throw new Error ("Map " + this.structure.id + " has a duplicate layer " + id);
				}
				
				this.layersMap[id] = layer;
				this.layersList.push (layer);
				
				this._createLayerIndex (layer);
			}
		}
	});
});