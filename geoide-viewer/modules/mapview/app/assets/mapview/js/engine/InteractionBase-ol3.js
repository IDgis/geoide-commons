define ([
	'dojo/_base/declare'
], function (
	declare
) {
	
	return declare ([], {
		_interactions: null,
		
		_enable: function (engine) {
			if (this._interactions) {
				this._disable (engine);
			}
			
			this._interactions = this._createInteractions (engine);
			
			for (var i = 0; i < this._interactions.length; ++ i) {
				engine.olMap.addInteraction (this._interactions[i]);
			}
		},
		
		_disable: function (engine) {
			if (!this._interactions) {
				return;
			}
			
			for (var i = 0; i < this._interactions.length; ++ i) {
				engine.olMap.removeInteraction (this._interactions[i]);
			}
			
			this._interactions = null;
		},
		
		_createInteractions: function (engine) {
			throw new Error ('_createInteractions not implemented');
		}
	});
});