require ([
	'dojo/_base/lang',
	
	'dojo/query',
	
	'put-selector/put',
	
	'geoide-map/Viewer',
	'geoide-core/Model',
	'geoide-core/linkedCopy',
	'geoide-core/map/Map',
	
	'geoide-map/interaction/Navigation',
	'geoide-map/interaction/KeyboardNavigation',
	'geoide-map/interaction/DragZoom',
	'geoide-map/interaction/DrawGeometry',
	'geoide-map/interaction/Click',
	
	'dojo/domReady!'
], function (
	lang,
	
	query,
	
	put, 
	
	Viewer,
	Model,
	linkedCopy,
	Map,
	
	Navigation,
	KeyboardNavigation,
	DragZoom,
	DrawGeometry,
	Click
) {
	
	var drawGeometry = new DrawGeometry ({
		type: 'polygon'
	});
	
	drawGeometry.on ('drawend', function (e) {
		console.log ('Geometry drawn: ', e.geometry);
	});
	
	var click = new Click ();
	
	click.on ('click', function (e) {
		console.log ('Clicked: ', e.coordinate);
	});
	
	var viewers = query ('.js-geoide-viewer').map (function (viewerNode) {
		var viewer = new Viewer (viewerNode, {
			resolutions: [3440.640, 1720.320, 860.160, 430.080, 215.040, 107.520, 53.760, 26.880, 13.440, 6.720, 3.360, 1.680, 0.840, 0.420, 0.210],
			interactions: [new Navigation (), new KeyboardNavigation (), click]
		});
		
		viewer.on ('moveEnd', function (e) {
			console.log ('Move end: ', e.viewer.get ('center'), e.viewer.get ('resolution'));
		});
		
		viewer.startup ();
		
		return viewer;
	});
	
	window.setLayerVisible = function (layerId, visible) {
		viewers[0].setLayerState (layerId, 'visible', visible).then (function () {
			console.log ('Visibility change in effect.');
		});
	};
	
	window.setLayerState = function (layerId, key, value) {
		viewers[0].setLayerState (layerId, key, value).then (function () {
			console.log ('State change in effect.');
		});
	};
	
	window.setViewerAttribute = function (name, value) {
		viewers[0].set (name, value);
	};
	
	window.getViewerAttribute = function (name) {
		return viewers[0].get (name);
	};
	
	window.zoomTo = function (center, resolution, animate) {
		return viewers[0].zoomTo (center, resolution, animate);
	};
	
	window.zoomToExtent = function (extent, animate) {
		return viewers[0].zoomToExtent (extent, animate);
	};

	window.query = function (options) {
		viewers[0].query (options).then (function (data) {
			console.log ("Query succeeded: ", data);
		}, function (error) {
			console.log ("Query failed: ", error);
		});
	};
	
	window.model = new Model ({
		id: 'my-model',
		layers: [
			{
				id: 'layer-1'
			},
			{
				id: 'layer-2'
			}
		]
	});
	
	window.model2 = linkedCopy (window.model);
	
	var value = {"id":"test-map","label":"Test map","layers":[{"id":"layer-1","label":"BRT achtergrondkaart"},{"id":"layer-2","label":"LPG"}]};
	window.mapModel = new Map (value);
	window.mapCopy = linkedCopy (window.mapModel);
});