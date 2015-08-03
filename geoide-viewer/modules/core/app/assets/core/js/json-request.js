define ([
	'dojo/_base/lang',
	'dojo/request/xhr',
	'dojo/Deferred',
	'dojo/json'
], function (lang, xhr, Deferred, json) {

	function doRequestJson (/*String*/ url, /*Object?*/ options) {
		var fullOptions = lang.mixin ({ handleAs: 'json' }, options || { }),
			def = new Deferred ();
		
		xhr (url, fullOptions).then (function (data) {
			def.resolve (data);
		}, function (error) {
			if (!error.response || !error.response.text) {
				def.reject (error);
				return;
			}
			
			try {
				def.reject (json.parse (error.response.text));
			} catch (e) {
				def.reject (error);
			}
		});
		
		return def;
	}
	
	return {
		get: function (/*String*/ url, /*Object?*/ options) {
			return doRequestJson (url, lang.mixin ({ method: 'GET' }, options || { }));
		},
		
		post: function (/*String*/ url, /*Object?*/ options) {
			return doRequestJson (url, lang.mixin ({ method: 'POST' }, options || { }));
		}
	};
});