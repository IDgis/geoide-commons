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
				new ol.interaction.DragRotate (), 
				new ol.interaction.DoubleClickZoom (), 
				new ol.interaction.DragPan ({
					kinetic: new ol.Kinetic (-0.005, 0.05, 100)
				}), 
				new ol.interaction.PinchRotate (),
				new ol.interaction.PinchZoom (),
				new ol.interaction.MouseWheelZoom (),
				new ol.interaction.DragZoom ()
			];
		}
	});
});