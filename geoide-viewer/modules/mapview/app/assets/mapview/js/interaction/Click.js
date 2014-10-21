define ([
	'dojo/_base/declare',
	'dojo/Evented',
	'./Interaction',
	'dojo/has!config-OpenLayers-3?../engine/Click-ol3:../engine/Click-ol2'
], function (
	declare,
	Evented,
	Interaction,
	Engine
) {

	return declare ([Interaction, Evented, Engine], {
		
	});
});