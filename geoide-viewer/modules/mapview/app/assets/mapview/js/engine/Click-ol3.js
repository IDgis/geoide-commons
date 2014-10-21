define ([
	'dojo/_base/declare'
], function (declare) {
	return declare ([], {
		_handle: null,
		
		_enable: function (engine) {
			if (this._handle) {
				return;
			}
			
			var self = this;
			
			this._handle = engine.olMap.on ('singleclick', function (e) {
				self.emit ('click', { coordinate: e.coordinate });
			});
		},
		
		_disable: function (engine) {
			if (!this._handle) {
				return;
			}
			
			engine.olMap.unByKey (this._handle);
			
			this._handle = null;
		}
	});
});