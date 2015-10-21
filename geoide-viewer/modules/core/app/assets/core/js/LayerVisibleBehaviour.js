define ([
   'dojo/_base/declare',
   'dojo/_base/lang',
   
   'dojo/dom-attr',
   'dojo/on',
   'dojo/query',
   
   'dojo/when',
   'dojo/Deferred',
   
   './DOMBehaviour',
   'geoide-core/map/MapBehaviour'
],
function(
	declare,
	lang,
	
	domAttr,
	on,
	query,
	
	when,
	Deferred,
	DOMBehaviour,
	MapBehaviour
){
	return declare([DOMBehaviour,MapBehaviour],{
		
		
		startup: function(){
			
			var promise = this.inherited(arguments),
				deferred = new Deferred();
			
			promise.then(lang.hitch(this, function() {
				
				var thisObject = this;
				
				when (this.map, function(map) {
					var layerRefs = map.get('layerRefList');
					layerRefs.forEach (function (layerRef) {
						layerRef.get ('state').watch ('visible', function(property, oldValue, newValue) { 
								thisObject.setCheckBox(layerRef, newValue);
						});
						
					});
				});
				
				on (this.node, '.toc-checkbox:click', function (e) {
					e.preventDefault();
					e.stopPropagation();
					thisObject.switchVisibility(this); 
				});
				
				deferred.resolve();
			}));
			
			
			
			return deferred;
			
		},
	
		switchVisibility: function (checkboxNode) {
			
			var layerRefId = this.getLayerRefId(checkboxNode);
			
			when (this.map, function(map) {
				var layerRef = map.get ('layerRefDictionary').get (layerRefId);
				var layerRefVisible = layerRef.get ('state').get ('visible');
				layerRef.get ('state').set ('visible', !layerRefVisible);
			});
			
		},
		
		getLayerRefId: function (node) {
			var parent = node;
			
			while (parent && !domAttr.get(parent, "data-layerref-id")) {
				parent = parent.parentNode;
			}
			var layerRefId = domAttr.get(parent, "data-layerref-id");
			return layerRefId;
			
		},

		setCheckBox: function(layerRef, visible) {
			window.setTimeout (function () {
				query ('*[data-layerref-id="' + layerRef.get ('id') + '"]', this.node).forEach (function (layerRefNode) {
					query ('> label .toc-checkbox', layerRefNode).forEach(function (checkBoxNode) {
						checkBoxNode.checked = visible;					
					});
				});
			}, 0);
		}
	
		
	});
	
});