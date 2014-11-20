define (['dojo/_base/declare', 'dojo/_base/lang'], function (declare, lang) {
	
	return declare ([], {
		_defaultConfig: { },
		
		constructor: function () {

			var config = lang.mixin ({ }, arguments.length > 0 ? arguments[arguments.length - 1] : defaultConfig);
			
			this._parseConfig (config);
		},

		/**
		 * Parses the configuration for this behaviour.
		 * 
		 * @return The configuration object with the configuration objects that have
		 *         been processed by the behaviour removed.
		 */
		_parseConfig: function (config) {
			return config;
		}
	});
});
