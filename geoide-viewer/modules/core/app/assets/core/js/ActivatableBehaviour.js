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
		onlyOneActive: false,
		
		startup: function () {
			var promise = this.inherited (arguments),
				deferred = new Deferred ();
			var thisObject = this;		
			promise.then (lang.hitch (this, function () {
				//Startup ActivatableBehaviour
				on (this.node, '.activatable-setter:click', function(e){
					e.preventDefault();
					e.stopPropagation();
					
					if (thisObject.onlyOneActive) {
						var activatableNodes = query('.activatable');
						for(k=0; k < activatableNodes.length; k++){
							if(domClass.contains(activatableNodes[k], 'active')){
                              domClass.toggle (activatableNodes[k], 'active');
                            }
						}
					}
					
					var parent = this;
					while (parent && !domClass.contains (parent, 'activatable')) {
						parent = parent.parentNode;
					}
					if (parent) {
						var layerId = parent.dataset.layerId;
						domClass.toggle (parent, 'active');
						
					}
					
				});
				
				deferred.resolve ();
				
			}));
			
			return deferred;
		
		},
		
		setOnlyOneActive: function (one) {
			this.onlyOneActive = one;
		}
		
		
	});	
});
	