define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	
	'dojo/dom-attr',
	
	'dojo/Deferred',
	'dojo/when',
	
	'../ConfigurableBehaviour',
	'./registry'
 ], 
 function (
	declare,
	lang,
	
	domAttr,
	
	Deferred,
	when,
	
	ConfigurableBehaviour,
	registry
  ) {
	
	return declare([ConfigurableBehaviour], {
		mapId: null,
		
		map: null,
		
		startup: function () {
			var def = new Deferred ();
			
			this.inherited (arguments).then (lang.hitch (this, function () {
				def.resolve ();
			}));

			if (!this.map) {
				this.map = registry.map (this.mapId);
				
				when (this.map, lang.hitch (this, function (map) {
					this.map = map; 
					console.log(this.map);
				}));
			}
			
			return def;
		},
		
		_parseConfig: function (config) {
			config = this.inherited (arguments);
			
			if ('map' in config) {
				this.map = config.map;
				delete config.map;
			}
			if ('mapId' in config) {
				this.mapId = config.mapId;
				delete config.mapId;
			} else if ('node' in this && this.node) {
				this.mapId = domAttr.get (this.node, 'data-geoide-map');
			}
			
			return config;
		}
	}); 
	
	
	
});