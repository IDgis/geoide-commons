define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	
	'./StatefulBase',
	'./StatefulObject',
	'./StatefulArray'
], function (
	declare, 
	lang,
	
	StatefulBase,
	StatefulObject,
	StatefulArray
) {
	var DefaultStatefulObject, DefaultStatefulArray;

	var defaultWrap = function (value, self, propertyName) {
		if (typeof value == 'object') {
			if (value.isInstanceOf && value.isInstanceOf (StatefulBase)) {
				return value;
			} else if ('length' in value) {
				// Wrap empty arrays or arrays containing objects:
				if (value.length === 0 || typeof value[0] == 'object') {
					return new DefaultStatefulArray (value);
				} else {
					return value;
				}
			} else {
				return new DefaultStatefulObject (value);
			}
		}
		
		return value;
	};	
	
	DefaultStatefulObject = declare ([StatefulObject], {
		_wrap: defaultWrap,
		
		constructor: function (content) {
			this._buildContent (content);
		}
	});
	
	DefaultStatefulArray = declare ([StatefulArray], {
		_wrap: defaultWrap,
		
		constructor: function (content) {
			this._buildContent (content);
		}
	});
	
	return declare ([DefaultStatefulObject], {
		_wrap: defaultWrap,
		
		constructor: function (content) {
		}
	});
});