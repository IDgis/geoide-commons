define ([

	'dojo/_base/declare',
	
	'dojo/query',
	'dojo/dom', 
	
	'dojo/Deferred'
], 

function(
	declare,
	
	query,
	dom,
	 
	Deferred
) {
	
	return declare (null, {
		node: null,
		
		constructor: function (nodeOrSelector){
			console.log ('DomBehaviour');
			this.node = query (nodeOrSelector)[0];
		},
		
		startup: function () {
			var promise = new Deferred ();
			
			promise.resolve ();
			
			return promise;
		}
	});		
	
});