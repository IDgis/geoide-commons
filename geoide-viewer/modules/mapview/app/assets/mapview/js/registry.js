define ([
	'dojo/_base/lang',
	
	'dojo/request/xhr',
	
	'dojo/Deferred',
	
	'./Map'
], function (
	lang,
	xhr,
	Deferred,
	Map
) {
	var maps = { };
	
	function createMap (mapId, jsonData) {
		return new Map (jsonData);
	}
	
	function getMap (mapId) {
		if (maps[mapId]) {
			return maps[mapId];
		}

		var mapPromise = new Deferred ();
		
		xhr.get (geoideViewerRoutes.controllers.mapview.MapConfiguration.mapStructure (mapId).url, {
			handleAs: 'json'
		}).then (function (data) {
			var map = createMap (mapId, data);
			maps[mapId] = map;
			mapPromise.resolve (map);
		}, function (error) {
			mapPromise.reject (error);
		});
		
		maps[mapId] = mapPromise;
		
		return mapPromise;
	}
	
	var layerTypes = { };
	
	function getLayerType (layerTypeName) {
		layerTypeName = ('' + layerTypeName).toLowerCase ();
		
		if (!(layerTypeName in layerTypes)) {
			throw new Error ('Layer type ' + layerTypeName + ' is undefined.');
		}
		
		return layerTypes[layerTypeName];
	}
	
	function registerLayerType (layerTypeName, layerType) {
		layerTypeName = ('' + layerTypeName).toLowerCase ();
		
		layerTypes[layerTypeName] = layerType;
	}
	
	var registry = {
		map: getMap,
		layerType: getLayerType,
		registerLayerType: registerLayerType
	};
	
	return registry;
});