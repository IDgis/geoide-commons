define ([
	'dojo/_base/declare',
	
	'./Model'
], function (
	declare,
	
	Model
) {
	var LinkedObject = declare ([], {
		_source: null,
		
		constructor: function (source) {
			
		},
		
		get: function () {
			
		},
		
		set: function () {
			
		},
		
		watch: function () {
			
		},
		
		extract: function () {
			
		}
	});
	
	function linkedCopy (object) {
		if (typeof object != 'object' || !('watch' in object)) {
			throw new Error ('Linked copies can be created from watchable objects only.');
		}
		
		
	}
	
	return linkedCopy;
});