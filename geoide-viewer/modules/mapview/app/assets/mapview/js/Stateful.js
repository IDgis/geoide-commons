define ([
	'dojo/_base/declare'
], function (
	declare
) {
	return declare ([], {
		_watchHandles: null,
		
		set: function (name, value) {
			this._set (name, value);
			return this;
		},
		
		get: function (name) {
			return this._get (name);
		},
		
		_set: function (name, value) {
			var setterName = '_' + name + 'Setter';
			if (setterName in this && this[setterName].apply) {
				this[setterName] (value);
			} else {
				this[name] = value;
			}
		},
		
		_get: function (name) {
			var getterName = '_' + name + 'Getter';
			if (getterName in this && this[getterName].apply) {
				return this[getterName] ();
			} else {
				return this[name];
			}
		},
		
		watch: function (propertyName, callback) {
			if (!this._watchHandles) {
				this._watchHandles = { };
			}
			
			if (!(propertyName in this._watchHandles)) {
				this._watchHandles[propertyName] = [ ];
			}
			
			var handle = { 
				callback: callback,
			};
			
			this._watchHandles[propertyName].push (handle);
			
			handle.remove = function () {
				throw new Error ("Not yet implemented");
			};

			throw new Error ("Not yet implemented");
		}
	});
});