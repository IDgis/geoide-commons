define (['dojo/_base/declare'], function (declare) {
	
	/**
	 * Base class for interactions. Copies the contents of the config object to attributes of the interaction.
	 */
	return declare ([], {
		constructor: function (config) {
			if (!config) {
				return;
			}
			
			for (var i in config) {
				if (!config.hasOwnProperty (i)) {
					continue;
				}
				
				this[i] = config[i];
			}
		}
	});
});