// Define the "window" object and let it point to the global scope:
var window = this;

// Require js:
var requireJsCache = { };

function combine (basePath, path) {
	while (true) {
		if (path.substring (0, 2) == "./") {
			path = path.substring (2);
		} else if (path.substring (0, 3) == "../") {
			var n = basePath.lastIndexOf ('/');
			if (n >= 0) {
				basePath = basePath.substring (0, n);
			}
			path = path.substring (3);
		} else {
			break;
		}
	}
	
	return basePath + "/" + path;
}

function doRequire (dep, base) {
	// Make the path absolute:
	if (base && dep.substring (0, 1) == '.') {
		var n = base.lastIndexOf ('/');
		
		if (n >= 0) {
			dep = combine (base.substring (0, n), dep);
		} else {
			dep = combine ('', dep);
		}
	}
	
	// Check the cache:
	if (dep in requireJsCache) {
		return requireJsCache[dep];
	}
	
	// Load the dependency:
	var result = loader.load (dep),
		finalPath = result[0],
		content = result[1];
	
	var exports = (function () {
		var require = function (dep) {
			return doRequire (dep, finalPath);
		};
		
		var module = { };
		
		eval (content);

		return module.exports;
	}) ();
	
	requireJsCache[dep] = exports;
	
	return exports;
}

function require (dep) {
	return doRequire (dep);
}

// Mocked Promise implementation:
function Promise () { 
}

Promise.prototype.resolve = function (a) { 
}; 

Promise.prototype.reject = function (a) { 
};			

// Utility for printing errors:
function printError (error) {
}

// Require the less compiler:
var less = require ('less')(/*null, [fs]*/);

function toArray (list) {
	var StringArray = Java.type ("java.lang.String[]");
	
	if (!list) {
		return new StringArray (0);
	}
	
	var array = new StringArray (list.length);
	
	for (var i = 0; i < list.length; ++ i) {
		array[i] = list[i];
	}
	
	return array;
}

// Entrypoint to be called from the Java code:
function lessCompile (input, variables) {
	var compileError, compileResult;
	
	var options = {
		processImports: false,
		compress: true
	};
	
	if (variables) {
		var vars = { };
		for (var i in variables) {
			vars[i] = variables[i];
		}
		options.globalVars = vars;
	}
	
	less.render (
		input, 
		options, 
		function (error, result) {
			if (error) {
				printError (error);
				compileError = error;
				return;
			}

			compileResult = result;
		}
	);
	
	if (compileError) {
		return new (Java.type ('nl.idgis.geoide.commons.report.layout.less.LessCompilationException')) (
			compileError.messge,
			compileError.filename,
			compileError.line === null ? -1 : compileError.line,
			compileError.column === null ? -1 : compileError.column,
			toArray (compileError.extract)
		);
	}
	
	return compileResult.css;
}