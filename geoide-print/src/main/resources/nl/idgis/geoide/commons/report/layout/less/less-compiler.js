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
	var result = req.load (dep),
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
	print ('Resolve: ', a); 
}; 

Promise.prototype.reject = function (a) { 
	print ('Reject: ', a); 
};			

// Utility for printing errors:
function printError (error) {
	print (error.filename + ":" + error.line + ":" + error.column + ": " + error.message + " (" + error.type + "): ");
	print (error.stack);
}

// Require the less compiler:
require ('less/tree/element');

var old = req.requireJsCache['less/tree/element'];
req.requireJsCache['less/tree/element'] = function (c, value) {
	print ("Element: " + c + ", " + value + ", " + (c instanceof require ('less/tree/combinator')));
	for (var i in c) {
		print (i + ": " + c[i]);
	}
	
	old.apply (this, arguments);
	
	print ("End element");
};

var less = require ('less')(/*null, [fs]*/);

// Entrypoint to be called from the Java code:
function lessCompile (input) {
	var compileError, compileResult;
	
	less.render (
		input, 
		{ 
			processImports: false 
		}, 
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
		throw compileError;
	}
	
	return compileResult.css;
}

print (lessCompile (".a { display: block; }"))