define (['dojo/_base/declare'], function (declare) {
	
	return declare ([], {
		_schema: null,
		
		_wrap: function (value, self, propertyName) {
			throw new Error ("Wrap not implemented for this stateful object");
		},
		
		_getSchemaProperty: function (name) {
			if (this._schema && name in this._schema) {
				return this._schema[name];
			}
			
			return { };
		}
	});
});