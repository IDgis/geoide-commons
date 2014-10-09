define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	
	'./StatefulObject'
], function (
	declare,
) {
	var LinkedObject = declare ([StatefulObject], {
		_source: null,
		_watchHandle: null,
		
		constructor: function (source) {
			this._source = source;
			this._watchHandle = this._source.watch (lang.hitch (this, function (name, oldValue, newValue) {
				
			}));
		},
		
		/**
		 * Returns a value from this object if this object has a value, otherwise delegates to
		 * the source object.
		 */
		get: function (name) {
			if (name in this._content[name]) {
				return this._content[name];
			}
			
			var value = this._source.get (name);
			
			if (typeof value == 'object' && value.watch) {
				value = linkedCopy (value);
				this._content[name] value;
			}
			
			return value;
		},
		
		extract: function () {
			return lang.mixin (lang.mixin ({ }, this._source.extract ()), this.inherited (arguments));
		}
	});
	
	function linkedCopy (object) {
		if (typeof object != 'object' || !('watch' in object)) {
			throw new Error ('Linked copies can be created from watchable objects only.');
		}
		
		
	}
	
	return linkedCopy;
});