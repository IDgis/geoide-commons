define ([
   'dojo/_base/declare',
   'dojo/_base/lang',
   
   'dojo/dom-attr',
   'dojo/on',
   
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
				on (this.node, '.toc-checkbox:click', function (e) {
					e.preventDefault();
					e.stopPropagation();
					
					var parent = this;
					while (parent && !parent.dataset.layerId) {
						parent = parent.parentNode;
					}
					var layerId = parent.dataset.layerId;
					when (thisObject.map, function(map) {
						var layer = map.get ('layerDictionary').get (layerId);
						var layerVisible = layer.get ('state').get ('visible');
						layer.get ('state').set ('visible', !layerVisible);
					});
					
				});
				deferred.resolve();
			}));
			
			return deferred;
		}
		
		
	});
	
});