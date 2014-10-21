define ([
	'dojo/_base/declare',
	
	'dojo/Evented',
	
	'./Interaction',
	'dojo/has!config-OpenLayers-3?../engine/DrawGeometry-ol3:../engine/DrawGeometry-ol2'
], function (
	declare,
	Evented,
	Interaction,
	Engine
) {

	return declare ([Interaction, Evented, Engine], {
		type: 'point',
		format: 'geojson'
	});
});