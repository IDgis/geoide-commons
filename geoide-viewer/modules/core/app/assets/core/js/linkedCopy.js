define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	
	'./StatefulObject',
	'./StatefulArray',
	'./Model'
], function (
	declare,
	lang,
	
	StatefulObject,
	StatefulArray,
	Model
) {
	var LinkedObject, LinkedArray;
	
	function linkedCopy (object) {
		if (typeof object != 'object' || (!('watch' in object) && !('watchElements' in object))) {
			throw new Error ('Linked copies can be created from watchable objects only.');
		}

		if (object.watchElements) {
			return new LinkedArray (object);
		} else {
			return new LinkedObject (object);
		}
	}
	
	var linkedObjectWrap = Model.prototype._wrap;
	
	/**
	 * - Setting a value on the copy will only update the copy, the source remains unchanged.
	 * - Changes on the source overwrite changes on the copy.
	 * - Watch events on the source are delegated to watchers that have registered on the copy.
	 * - Stateful objects or arrays inside the source are mirrored by stateful copies inside the copy (lazily).
	 * - Invoking extract on the copy will mix the values of the copy in the values of the original.
	 */
	LinkedObject = declare ([StatefulObject], {
		_source: null,
		_watchHandle: null,
		_wrap: linkedObjectWrap,
		
		constructor: function (source) {
			this._source = source;
			this._watchHandle = this._source.watch (lang.hitch (this, function (name, oldValue, newValue) {
				// Delete the property from this copy if it is modified in the source:
				var modified = true;
				if (name in this._content) {
					var currentValue = this._content[name];
					
					// Suppress the watch event if the value at the source matches the value previously set on the copy:
					if (currentValue === newValue) {
						modified = false;
					} else {
						oldValue = currentValue;
					}
					
					if (typeof currentValue == 'object' && currentValue.unlink) {
						currentValue.unlink ();
					}
					
					delete this._content[name];
				}
				
				if (modified) {
					// Wrap the new value:
					if (typeof newValue == 'object' && (newValue.watch || newValue.watchElements)) {
						newValue  = linkedCopy (newValue);
						this._content[name] = newValue;
					}
					
					// Send the event to watchers on this object:
					this._sendWatch (name, oldValue, newValue);
				}
			}));
		},
		
		_getSchemaProperty: function (name) {
			return this._source._getSchemaProperty (name);
		},
		
		/**
		 * Returns a value from this object if this object has a value, otherwise delegates to
		 * the source object.
		 */
		get: function (name) {
			if (name in this._content) {
				return this._content[name];
			}
			
			var value = this._source.get (name);
			
			if (typeof value == 'object' && (value.watch || value.watchElements)) {
				value = linkedCopy (value);
				this._content[name] = value;
			}
			
			return value;
		},
		
		has: function (name) {
			if (name in this._content) {
				return true;
			}
			
			return this._source.has (name);
		},
		
		_set: function (name, value) {
			if (name in this._content) {
				var oldValue = this._content[name];
				if (typeof oldValue == 'object' && oldValue.unlink) {
					oldValue.unlink ();
				}
			}
			
			this._content[name] = value;
		},
		
		extract: function () {
			return lang.mixin (lang.mixin ({ }, this._source.extract ()), this.inherited (arguments));
		},
		
		unlink: function () {
			if (this._watchHandle !== null) {
				this._watchHandle.unwatch ();
				this._watchHandle = null;
			}
		}
	});
	
	/**
	 * - The copy can be modified without changing the original.
	 * - Any change on the original replaces the entire copy with the contents of the source.
	 * - Watch events on the source are always translated to watch events that replace the entire array on the copy.
	 * - Watch events when modifying the copy fire normally.
	 */
	LinkedArray = declare ([StatefulArray], {
		_source: null,
		_watchHandle: null,
		_wrap: linkedObjectWrap,

		constructor: function (source) {
			this._source = source;
			this._watchHandle = source.watchElements (lang.hitch (this, function (index, /*Array*/removals, /*Array*/adds) {
				var oldContent = this._content;

				// Refresh the list from the source:
				this._makeCopy ();
				
				// Fire a change event for the entire array:
				this._fireWatch (0, oldContent, this._content);
			}));
			
			this._makeCopy ();
		},

		_getSchemaProperty: function (name) {
			return this._source._getSchemaProperty (name);
		},
		
		_makeCopy: function () {
			var i, value;
			
			// Unlink previously created linked copies:
			if (this._content) {
				for (i = 0; i < this._content.length; ++ i) {
					value = this._content[i];
					
					if (typeof value == 'object' && value.unlink) {
						value.unlink ();
					}
				}
			}
			
			var l = this._source.length ();
			
			this._content = [ ];
			
			for (i = 0; i < l; ++ i) {
				value = this._source.get (i);
				
				if (typeof value == 'object' && (value.watch || value.watchElements)) {
					value = linkedCopy (value);
				}
				
				this._content[i] = value;
			}
		},
		
		unlink: function () {
			if (this._watchHandle !== null) {
				this._watchHandle.unwatch ();
				this._watchHandle = null;
			}
		}
	});
	
	return linkedCopy;
});