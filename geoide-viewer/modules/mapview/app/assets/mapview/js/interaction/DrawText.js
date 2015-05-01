define ([
	'dojo/_base/declare',

	'./DrawGeometry',
	'../EditableOverlay',
	
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
		
		_handle: null,
		
		_enable: function (engine) {
			this.inherited (arguments);
			
			this._handle = this.on ('drawend', function (e) {
				var overlay = new Overlay ({
					feature: e.feature,
					text: '',
					height: 40,
					offset: [50, 50]
				});
				overlay.update ();
				
				engine.olMap.addOverlay (overlay._overlay);
				
				overlay.edit ();
			});
		},
		
		_disable: function (engine) {
			this.inherited (arguments);
			
			if (this._handle) {
				this._handle.remove ();
			}
		}
	});
});