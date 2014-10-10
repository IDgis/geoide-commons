define ([
	'dojo/_base/declare',
	'./InteractionBase-ol3'
], function (
	declare,
	InteractionBase
) {
	
	return declare ([InteractionBase], {
		_createInteractions: function (engine) {
			return [
				new ol.interaction.DragZoom ({ condition: ol.events.condition.always })
			];
		}
	});
});