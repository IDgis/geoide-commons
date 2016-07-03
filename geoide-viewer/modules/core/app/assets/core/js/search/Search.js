define ([
	'dojo/_base/declare',
	'dojo/on',
	'dojo/dom',
	'dojo/dom-class',
	'dojo/query',
	'dojo/when',
	'dojo/Deferred',
	
	'geoide-core/map/MapBehaviour',
	'geoide-core/DOMBehaviour'
], function (
		declare,
		on,
		dom,
		domClass,
		query,
		when,
		Deferred,
		
		MapBehaviour,
		DOMBehaviour
	
) {
	return declare ([DOMBehaviour], {
		
		qOptions:null,
		
		constructor: function (searchNode, viewer) {
			
	
			
			viewer.map.get('searchTemplateList').forEach (function(q) {
				console.log(q);
			    console.log(q.get('label'));
			    
			  
		});
	}});
	
});
