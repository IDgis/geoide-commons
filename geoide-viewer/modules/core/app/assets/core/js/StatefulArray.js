define ([
	'dojo/_base/declare',
	'./StatefulBase'
], function (
	declare,
	StatefulBase
) {
	return declare ([StatefulBase], {
		_content: null,
		_watches: null,
		
		constructor: function (content) {
			this._content = [ ];
		},
		
		_buildContent: function (rootArray) {
			this._content = [ ];
			
			for (var i = 0; i < rootArray.length; ++ i) {
				this._content.push (this._wrap (rootArray[i]));
			}
		},
		
		length: function () {
			return this._content.length;
		},
		
		get: function (index) {
			return this._content[index];
		},
		
		set: function (index, value) {
			if (index < 0) {
				throw new Error ("index must be >= 0");
			}

			value = this._wrap (value);
			
			if (index < this._content.length) {
				// Replace a value:
				var previous = this._content[index];
				if (value !== previous) {
					this._content[index] = value;
					this._fireWatch (index, [ previous ], [ value ]);
				}
			} else {
				// Append a value:
				var adds = [ ],
					startIndex = this._content.length;
				
				while (this._content.length < index) {
					adds.push (null);
					this._content.push (null);
				}
				
				adds.push (value);
				this._content.push (value);
				
				this._fireWatch (startIndex, [ ], adds);
			}
			
			return this;
		},

		/**
		 * Similar to Array.prototype.pop
		 */
		pop: function () {
			if (this._content.length <= 0) {
				return undefined;
			}
			
			var removes = [ this._content.pop () ];
			
			this._fireWatch (this._content.length, removes, [ ]);
			
			return removes[0];
		},
		
		/**
		 * Similar to Array.prototype.push
		 */
		push: function (value) {
			value = this._wrap (value);
			
			this._content.push (this._wrap (value));
			
			this._fireWatch (this._content.length - 1, [ ], [ value ]);
			
			return this._content.length;
		},

		/**
		 * Similar to Array.prototype.shift
		 */
		shift: function () {
			if (this._content.length <= 0) {
				return undefined;
			}
			
			var removed = this._content.shift ();
			
			this._fireWatch (0, [ removed ], [ ]);
			
			return removed;
		},
		
		/**
		 * Similar to Array.prototype.unshift
		 */
		unshift: function (value) {
			value = this._wrap (value);
			
			this._content.unshift (value);
			
			this._fireWatch (0, [ ], [ value ]);
			
			return this._content.length;
		},
		
		/**
		 * Similar to Array.prototype.splice
		 */
		splice: function (index, howMany) {
			var i;
			
			if (index < 0) {
				index = 0;
			}
			if (index > this._content.length) {
				index = this._content.length;
			}
			
			// List the removed elements:
			var removed = [ ];
			for (i = index; i < index + howMany && i < this._content.length; ++ i) {
				removed.push (this._content[index]);
			}
			
			// List the added elements:
			var args = [ index, howMany ],
				adds = [ ];
			for (i = 2; i < arguments.length; ++ i) {
				var v = this._wrap (arguments[i]);
				args.push (v);
				adds.push (v);
			}
			
			Array.prototype.splice.apply (this._content, args);
			
			this._fireWatch (index, removed, adds);
			
			return removed;
		},
		
		_fireWatch: function (index, removals, adds) {
			if (this._watches !== null) {
				for (var i = 0; i < this._watches.length; ++ i) {
					this._watches[i] (index, removals, adds);
				}
			}
		},
		
		/**
		 * WatchElements is interface compatible with dojox.mvc.StatefulArray. Callback takes the following arguments:
		 * - index: the start index of the change.
		 * - removals: list of items that have been removed at this index
		 * - adds: list of items that have been added at this index
		 */
		watchElements: function (callback) {
			if (this._watches === null) {
				this._watches = [ ];
			}
			
			var list = this._watches;
			
			this._watches.push (callback);
			
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
			var list = [ ];
			
			for (var i = 0; i < this._content.length; ++ i) {
				var value = this._content[i];
				
				if (typeof value == 'object' && value !== null && 'isInstanceOf' in value) {
					list.push (value.extract ());
				} else {
					list.push (value);
				}
			}
			
			return list;
		}
	});
});