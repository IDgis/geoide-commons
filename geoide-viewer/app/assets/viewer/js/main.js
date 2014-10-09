require ([
	'dojo/_base/lang',
	
	'dojo/query',
	
	'put-selector/put',
	
	'geoide-map/Viewer',
	'geoide-map/Model',
	
	'dojo/domReady!'
], function (
	lang,
	
	query,
	
	put, 
	
	Viewer,
	Model
) {
	
	var viewers = query ('.js-geoide-viewer').map (function (viewerNode) {
		var viewer = new Viewer (viewerNode, {
			resolutions: [3440.640, 1720.320, 860.160, 430.080, 215.040, 107.520, 53.760, 26.880, 13.440, 6.720, 3.360, 1.680, 0.840, 0.420, 0.210]
		});
		
		viewer.on ('moveEnd', function (e) {
			console.log ('Move end: ', e.viewer.get ('center'), e.viewer.get ('resolution'));
		});
		
		viewer.startup ();
		
		return viewer;
	});
	
	window.setLayerVisible = function (layerId, visible) {
		viewers[0].getLayerView (layerId).setVisible (visible).then (function () {
			console.log ('Visibility change in effect.');
		});
	};
	
	window.setLayerState = function (layerId, key, value) {
		viewers[0].getLayerView (layerId).setState (key, value).then (function () {
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
});