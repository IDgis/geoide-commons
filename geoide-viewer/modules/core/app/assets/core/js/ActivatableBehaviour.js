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
function (
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
				//Startup ActivatableBehaviour
				on (this.node, '.gi-toc-title:click', function(e){
					e.preventDefault();
					e.stopPropagation();
					var parent = this;
					while (parent && !domClass.contains (parent, 'activatable')) {
						parent = parent.parentNode;
					}
					if (parent) {
						var layerId = parent.dataset.layerId;
						//if ('map' in this && this.map) {
							console.log("Ik heb een layerId " + layerId);
						//}
						domClass.toggle (parent, 'active');
					}
					
				});
				
				deferred.resolve ();
				
			}));
			
			return deferred;
		
		}
		
	});	
});
	