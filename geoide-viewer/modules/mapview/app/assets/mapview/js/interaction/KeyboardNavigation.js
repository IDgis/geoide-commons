define ([
	'dojo/_base/declare',
	'./InteractionBase',
	
	'openlayers/ol-debug'
], function (
	declare,
	InteractionBase,
	
	ol
) {

	return declare ([InteractionBase], {
		_createInteractions: function (engine) {
			return [
				new ol.interaction.KeyboardPan (),
				new ol.interaction.KeyboardZoom ()
			];
		}
	});
});