define ([
	'dojo/_base/declare',
	'dojo/Evented',
	'./Interaction',
	'../engine/Click-ol3'
], function (
	declare,
	Evented,
	Interaction,
	Engine
) {

	/**
	 * Events:
	 * - click: invoked with an event object containing a single property: coordinate.
	 */
	return declare ([Interaction, Evented, Engine], {
		
	});
});