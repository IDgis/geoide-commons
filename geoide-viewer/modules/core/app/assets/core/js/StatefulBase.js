define (['dojo/_base/declare'], function (declare) {
	
	return declare ([], {
		_schema: null,
		
		_wrap: function (value) {
			if (!this._schema || !this._schema.wrap) {
				throw new Error ('No schema, or no wrap method');
			}
			
			return this._schema.wrap (value);
		}
	});
});