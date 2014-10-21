define (['dojo/_base/declare'], function (declare) {
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