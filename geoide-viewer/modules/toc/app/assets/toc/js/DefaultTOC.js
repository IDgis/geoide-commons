define ([
	'dojo/_base/declare',

	'geoide-core/ExplandableBehaviour',
    'geoide-core/ActivatableBehaviour',
    'geoide-core/map/MapBehaviour',
    'geoide-core/LayerVisibleBehaviour'
    
],

function(
	declare,
	
	ExplandableBehaviour,
	ActivatableBehaviour,
	MapBehaviour,
	LayerVisibleBehaviour
	
){
	
	return declare([ExplandableBehaviour, MapBehaviour, ActivatableBehaviour, LayerVisibleBehaviour], {
		
		
	});

});
