define ([
	'dojo/_base/declare'
], function (
	declare
) {
	
	return declare ([], {
		viewer: null,
		layerId: null,
		
		constructor: function (viewer, layerId) {
			this.viewer = viewer;
			this.layerId = layerId;
		},
		
		getViewer: function () {
			return this.viewer;
		},
		
		getLayerId: function () {
			return this.layerId;
		},
		
		setVisible: function (visible) {
			return this.setState ('visible', visible);
		},
		
		isVisible: function () {
			return this.getState ('visible');
		},
		
		setState: function (key, value) {
			return this.viewer.setLayerState (this.layerId, key, value);
		},
		
		getState: function (key) {
			return this.viewer.getLayerState (this.layerId, key);
		}
	});
});