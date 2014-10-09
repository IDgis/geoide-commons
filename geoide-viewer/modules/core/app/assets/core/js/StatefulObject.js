define (['dojo/_base/declare', './StatefulBase'], function (declare, StatefulBase) {
	return declare ([StatefulBase], {
		_content: content,
		_watchHandles: null,
		_globalWatchHandles: null,
		
		constructor: function (content) {
			this._buildContent (content);
		},
		
		_buildContent: function (rootObject) {
			this._content = { };
			
			for (var i in rootObject) {
				if (!(rootObject.hasOwnProperty (i))) {
					continue;
				}
				
				this._content[i] = this._wrap (rootObject[i]);
			}
		},
		
		get: function (name) {
			return this._content[name];
		},
		
		set: function (nameOrValues, optionalValue) {
			var i;
			
			if (typeof nameOrValues == 'object') {
				for (i in nameOrValues) {
					if (nameOrValues.hasOwnProperty (i)) {
						this.set (i, nameOrvalues[i]);
					}
				}
				return this;
			}
			
			var name = nameOrValues,
				value = this._wrap (optionalValue),
				previousValue = this.get (name);
			
			if (value !== previousValue) {
				this._content[name] = this._wrap (value);
				
				// Invoke global watch handles:
				if (this._globalWatchHandles !== null) {
					for (i = 0; i < this._globalWatchHandles.length; ++ i) {
						this._globalWatchHandles[i] (name, previousValue, value);
					}
				}
				
				// Invoke specific watch handles:
				if (this._watchHandles !== null && name in this._watchHandles) {
					for (i = 0; i < this._watchHandles[name].length; ++ i) {
						this._watchHandles[name][i] (name, previousValue, value);
					}
				}
			}
			
			return this;
		},
		
		watch: function (nameOrCallback, optionalCallback) {
			var list, callback;
			
			if (typeof nameOrCallback == 'function') {
				if (this._globalWatchHandles === null) {
					this._globalWatchHandles = { };
				}
				
				list = this._globalWatchHandles;
				callback = nameOrCallback;
			} else {
				if (this._watchHandles === null) {
					this._watchHandles = { };
				}
				if (!(nameOrCallback in this._watchHandles)) {
					this._watchHandles[nameOrCallback] = [ ];
				}
				
				list = this._watchHandles[nameOrCallback];
				callback = optionalCallback;
			}
			
			list.push (callback);
			
			var removeFn = function () {
				for (var i = 0; i < list.length; ++ i) {
					if (list[i] === callback) {
						list.splice (i, 1);
						return;
					}
				}
			};
			
			return {
				remove: removeFn,
				unwatch: removeFn
			};
		},
		
		extract: function () {
			var obj = { };
			
			for (var i in this._content) {
				if (!this._content.hasOwnProperty (i)) {
					continue;
				}
				
				var value = this._content[i];
				
				if (typeof value == 'object' && value !== null && 'isInstanceOf' in value) {
					obj[i] = value.extract ();
				} else {
					obj[i] = value;
				}
			}
			
			return obj;
		}
	});
});