define ([
	'dojo/_base/declare'
], function (
	declare
) {
	return declare ([], {
		_watchHandles: null,
		
		set: function (name, value) {
			var oldvalue = this.get(name);
			this._set (name, value);

			if (oldvalue !== value) {
				
				this._sendWatch(name, oldvalue, value);
			
				
			}
			
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
		
		_sendWatch: function (name, previousValue, value) {
			// Invoke specific watch handles:
			if (this._watchHandles !== null && name in this._watchHandles) {

				for (i = 0; i < this._watchHandles[name].length; ++ i) {
					this._watchHandles[name][i].callback (name, previousValue, value);
				}
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
				callback: callback
			};
			
			this._watchHandles[propertyName].push (handle);
			
			var list = this._watchHandles[propertyName];
			handle.remove = function () {
				for (var i = 0; i < list.length; ++ i) {
					
					if (list[i].callback === callback) {
						list.splice (i, 1);
					}	
				}	
			};
			
			return handle;
			

			//throw new Error ("Not yet implemented");
		}
	});
});