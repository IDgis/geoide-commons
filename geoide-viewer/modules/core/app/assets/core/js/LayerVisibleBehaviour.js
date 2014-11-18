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
			console.log("startup LayerVisibleBehaviour met " + this.mapId);
			
			var promise = this.inherited(arguments),
				deferred = new Deferred();
			
			promise.then(lang.hitch(this, function() {
				
				var thisObject = this;
				
				when (this.map, function(map) {
					var layers = map.get('layerList');
					layers.forEach (function (layer) {
						layer.get ('state').watch ('visible', function(property, oldValue, newValue) { 
								thisObject.setCheckBox(layer, newValue);
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
			
			var layerId = this.getLayerId(checkboxNode);
			
			when (this.map, function(map) {
				var layer = map.get ('layerDictionary').get (layerId);
				var layerVisible = layer.get ('state').get ('visible');
				layer.get ('state').set ('visible', !layerVisible);
			});
			
		},
		
		getLayerId: function (node) {
			var parent = node;
			
			while (parent && !parent.dataset.layerId) {
				parent = parent.parentNode;
			}
			var layerId = parent.dataset.layerId;
			return layerId;
			
		},

		setCheckBox: function(layer, visible) {
			window.setTimeout (function () {
				query ('*[data-layer-id="' + layer.get ('id') + '"]', this.node).forEach (function (layerNode) {
					query ('> label .toc-checkbox', layerNode).forEach(function (checkBoxNode) {
						checkBoxNode.checked = visible;					
					});
				});
			}, 0);
		}
	
		
	});
	
});