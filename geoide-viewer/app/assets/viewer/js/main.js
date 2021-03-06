require ([
	'dojo/_base/lang',
	
	'dojo/query',
	'dojo/dom',
	'dojo/on',
	'dojo/when',
	
	'put-selector/put',
	
	'geoide-map/Viewer',
	'geoide-core/Model',
	'geoide-core/linkedCopy',
	'geoide-core/map/Map',
	'geoide-core/search/Search',
	'geoide-toc/DefaultTOC',
	
	'geoide-map/interaction/Navigation',
	'geoide-map/interaction/KeyboardNavigation',
	'geoide-map/interaction/DragZoom',
	'geoide-map/interaction/DrawGeometry',
	'geoide-map/interaction/Click',
	'geoide-map/interaction/ModifyGeometry',
	'geoide-map/interaction/DrawText',
	
	'dojo/domReady!'
], function (
	lang,
	
	query,
	dom,
	on,
	when,
	
	put, 
	
	Viewer,
	Model,
	linkedCopy,
	Map,
	Search,
	TOC,
	Navigation,
	KeyboardNavigation,
	DragZoom,
	DrawGeometry,
	Click,
	ModifyGeometry,
	DrawText
) {
	
	var drawGeometry = new DrawGeometry ({
		type: 'polygon'
	});
	
	var dragZoom = new DragZoom ({

	});
	
	drawGeometry.on ('drawend', function (e) {
		console.log ('Geometry drawn: ', e.geometry);
	});
	
	
	var draw = new DrawGeometry ({type: 'point', format: 'wkt', modifier: 'shiftKey'});
	var draw2 = new DrawGeometry ({type: 'point', format: 'wkt', modifier: 'none'});
	
	draw.on ('drawend', function (e) {
		console.log("shiftKey " + e.geometry);
	});
	
	draw2.on ('drawend', function (e) {
		console.log("none " + e.geometry);
	});
	
	var click = new Click ();
	
	click.on ('click', function (e) {
		console.log ('Clicked: ', e.coordinate);
	});
	
	var viewer = null;
	
	var viewers = query ('.js-geoide-viewer').map (function (viewerNode) {
		viewer = new Viewer (viewerNode, {
			resolutions: [3440.640, 1720.320, 860.160, 430.080, 215.040, 107.520, 53.760, 26.880, 13.440, 6.720, 3.360, 1.680, 0.840, 0.420, 0.210, 0.140, 0.070, 0.028],
			interactions: [new Navigation (), new KeyboardNavigation ()]
		});
		
		viewer.startup ();
		return viewer;
	});
	
	var tocs = query('.gi-toc').map (function (tocNode) {
		var toc = new TOC (tocNode);
		
		toc.startup();
		
		return toc;
	});
	
	when (viewer.map, function() { 
		var qdescs = query('.js-query-descriptions-picker').map (function (qdescNode) {
			var qdesc = new Search (qdescNode, viewer);
			qdesc.startup();
			//toc.startup();
			
			return qdesc;
		});
	});	
	
	window.setLayerRefVisible = function (layerId, visible) {
		viewers[0].setLayerRefState (layerId, 'visible', visible).then (function () {
			console.log ('Visibility change in effect.');
		});
	};
	
	window.setLayerRefState = function (layerId, key, value) {
		viewers[0].setLayerRefState (layerId, key, value).then (function () {
			console.log ('State change in effect.');
		});
	};
	
	window.getLayerRefState = function (layerId, key) {
		return viewers[0].getLayerRefState (layerId, key);
	};
	
	window.setViewerAttribute = function (name, value) {
		viewers[0].set (name, value);
	};
	
	window.getViewerAttribute = function (name) {
		return viewers[0].get (name);
	};
	
	window.zoomTo = function (center, resolution, animate) {
		console.log("main zoomto");
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
	
	window.overlay = function (name) {
		return viewers[0].overlay (name);
	};
	
	window.getViewerState = function (options) {
		return viewers[0].getViewerState (options);
	};
	
	window.report = function (templateOptions, viewerStateOptions) {
		return viewers[0].report (templateOptions, viewerStateOptions);
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
	
	var value = {"id":"test-map","label":"Test map","layerRefs":[{"id":"1","layerid":"layer-1","label":"BRT achtergrondkaart"},{"id":"2","layerid":"layer-2","label":"LPG"}]};
	window.mapModel = new Map (value);
	window.mapCopy = linkedCopy (window.mapModel);
	
	// Redlining interactions:
	var redlineInteraction = null;
	
	function redline (interaction) {
		viewers[0].disableInteraction (draw);
		viewers[0].disableInteraction (draw2);
		if (redlineInteraction) {
			viewers[0].disableInteraction (redlineInteraction);
		}
		
		viewers[0].enableInteraction (interaction);
		redlineInteraction = interaction;
	}
	
	on (dom.byId ('draw-point'), 'click', function (e) {
		e.preventDefault ();
		e.stopPropagation ();
	
		redline (new DrawGeometry ({
			type: 'Point', 
			format: 'wkt',
			source: viewers[0].overlay ('redline').getSource ()
		}));
	});
	on (dom.byId ('draw-line'), 'click', function (e) {
		e.preventDefault ();
		e.stopPropagation ();
		
		redline (new DrawGeometry ({
			type: 'LineString', 
			format: 'wkt', 
			source: viewers[0].overlay ('redline').getSource ()
		}));
	});
	on (dom.byId ('draw-polygon'), 'click', function (e) {
		e.preventDefault ();
		e.stopPropagation ();
		
		redline (new DrawGeometry ({
			type: 'Polygon', 
			format: 'wkt', 
			source: viewers[0].overlay ('redline').getSource ()
		}));
	});
	on (dom.byId ('draw-text'), 'click', function (e) {
		e.preventDefault ();
		e.stopPropagation ();
		
		redline (new DrawText ({
			source: viewers[0].overlay ('redline').getSource ()
		}));
	});
	on (dom.byId ('edit-geometry'), 'click', function (e) {
		e.preventDefault ();
		e.stopPropagation ();
		
		redline (new ModifyGeometry ({
			source: viewers[0].overlay ('redline').getSource ()
		}));
	});
	on (dom.byId ('delete-features'), 'click', function (e) {
		e.preventDefault ();
		e.stopPropagation ();
		
		if (redlineInteraction && redlineInteraction.deleteSelected) {
			redlineInteraction.deleteSelected ();
		}
	});
	
	on (dom.byId ('interaction-drag-zoom'), 'click', function (e) {
		e.preventDefault ();
		e.stopPropagation ();
		
		viewers[0].enableInteraction (new DragZoom ());
	});
	
	on (dom.byId ('test'), 'click', function (e) {
		e.preventDefault ();
		e.stopPropagation ();
		viewer.set ('minResolution', 0.028);
		//viewer.set ('zoomPolicy', 'nearest');
		viewer.zoomTo([150000, 460000], 0.10);
		//viewer.zoomToExtent([150000, 460000, 150600, 460200]);
		//console.log(viewer.getCurrentExtent());
	});
	
	
});
