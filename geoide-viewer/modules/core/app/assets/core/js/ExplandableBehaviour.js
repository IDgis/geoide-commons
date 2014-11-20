define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	
	'dojo/on',
	'dojo/query',
	'dojo/dom', 
	'dojo/dom-class',
	
	'dojo/Deferred',
	
	'./DOMBehaviour'
], 

function(
	declare,
	lang,
	
	on,
	query,
	dom,
	domClass,
	 
	Deferred,
	
	DOMBehaviour 
) {
	
	return declare([DOMBehaviour],{
		startup: function () {
			var promise = this.inherited (arguments),
				deferred = new Deferred ();
			
			promise.then (lang.hitch (this, function () {
				// Startup ExpandableBehaviour
				on (this.node, '.expandable-icons:click', function (e) {
					e.preventDefault ();
					e.stopPropagation ();
					
					var parent = this;
					while (parent && !domClass.contains (parent, 'expandable')) {
						parent = parent.parentNode;
					}
					if (parent) {
						domClass.toggle (parent, 'expanded');
					}
				});
				
				deferred.resolve ();
			}));
			
			return deferred;
		}
	
	});
	
	
	
});