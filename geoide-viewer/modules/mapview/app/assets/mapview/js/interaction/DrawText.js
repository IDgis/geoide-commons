define ([
	'dojo/_base/declare',

	'./DrawGeometry',
	'../MoveableOverlay',
	
	'openlayers/ol'
], function (
	declare,
	
	DrawGeometry,
	Overlay,
	
	ol
) {
	function svg (name, attributes, parent) {
		var elem = document.createElementNS ('http://www.w3.org/2000/svg', name);
		
		if (attributes) {
			for (var i in attributes) {
				if (!attributes.hasOwnProperty (i)) {
					continue;
				}
				
				elem.setAttribute (i, attributes[i]);
			}
		}
		
		if (parent) {
			parent.appendChild (elem);
		}
		
		return elem;
	}
	
	return declare ([DrawGeometry], {
		type: 'point',
		format: 'Feature',
		
		_enable: function (engine) {
			this.inherited (arguments);
			
			this.on ('drawend', function (e) {
				console.log ('Drawing text at: ', e.geometry);
				var offset = [50, 50];

				var element = document.createElement ('div');
				element.style.position = 'absolute';
				element.style.width = '100%';
				element.style.height = '100%';
				element.style.top = '0px';
				element.style.left = '0px';
				element.style.backgroundColor = 'red';
				element.appendChild (document.createTextNode ('Hello, World!'));
				
				var o = new Overlay ({
					content: element
				});
				o.update ();
				
				var overlay = new ol.Overlay ({
					element: o._container,// container,
					autoPan: true,
					autoPanAnimation: {
						duration: 250
					},
					position: e.geometry.getFirstCoordinate (),
					stopEvent: false,
					insertFirst: false
				});
				
				engine.olMap.addOverlay (overlay);
			});
		},
		
		_disable: function (engine) {
			this.inherited (arguments);
		}
	});
});