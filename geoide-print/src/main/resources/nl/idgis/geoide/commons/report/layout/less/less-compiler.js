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
		
		var code = '(function () { var require = function (dep) { return doRequire (dep, "' 
			+ finalPath 
			+ '"); }; var module = { }; '
			+ content
			+ '\nreturn module.exports; })();';
			
		var require = function (dep) {
			return doRequire (dep, finalPath);
		};
		
		// var module = { };
		
		return load ({ script: code, name: finalPath + '.js' });
		//eval (content);

		// return module.exports;
	}) ();
	
	requireJsCache[dep] = exports;
	
	return exports;
}

function require (dep) {
	return doRequire (dep);
}

// Mocked Promise implementation:
function Promise () {
	this.callbacks = [ ];
	this.errbacks = [ ];
}

Promise.prototype.resolve = function (a) {
	if ('error' in this) {
		return;
	}
	
	this.value = a;
	for (var i = 0; i < this.callbacks.length; ++ i) {
		this.callbacks[i] (a);
	}
}; 

Promise.prototype.reject = function (a) {
	if ('value' in this) {
		return;
	}
	
	this.error = a;
	for (var i = 0; i < this.errbacks; ++ i) {
		this.errbacks[i] (a);
	}
};

Promise.then = function (callback, errback) {
	if (callback) {
		this.callbacks.push (callback);
		if ('value' in this) {
			callback (this.value);
		}
	}
	
	if (errback) {
		this.errbacks.push (errback);
		if ('error' in this) {
			errback (this.error);
		}
	}
};

// Utility for printing errors:
function printError (error) {
	print (error.stack);
}

// Filesystem implementation:
function Filesystem () {
}

Filesystem.prototype = new (require ('less/environment/abstract-file-manager')) ();

Filesystem.prototype.supportsSync = function(filename, currentDirectory, options, environment) {
	return true;
};

Filesystem.prototype.supports = function(filename, currentDirectory, options, environment) {
	return true;
};

Filesystem.prototype.loadFile = function(filename, currentDirectory, options, environment, callback) {
	var content = this.loadFileSync (filename, currentDirectory, options, environment);
	
	if (content.error) {
		callback (content.error);
	} else {
		callback (null, {
			contents: content.contents,
			filename: content.filename
		});
	}
};

Filesystem.prototype.loadFileSync = function(filename, currentDirectory, options, environment) {
	if (fileLoader) {
		var content = fileLoader.loadFile (filename, currentDirectory);

		if (content.isPresent ()) {
			return {
				filename: filename,
				contents: content.get ()
			};
		} else {
			return {
				error: new Error ('File not found: ' + filename)
			};
		}
	} else {
		return {
			error: new Error ('Imports not supported: no file loader present (' + filename + ')')
		};
	}
};

// Require the less compiler:
var less = require ('less')({ }, [new Filesystem ()]);

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

// Monkeypatch the import-sequencer. The finally block is executed twice in Nashorn when a break or
// return is triggered inside the while loop.
requireJsCache['less/visitors/import-sequencer'].prototype.tryRun = function () {
    this._currentDepth++;
    try {
        while(true) {
            while(this.imports.length > 0) {
                var importItem = this.imports[0];
                if (!importItem.isReady) {
                	-- this._currentDepth;
                    return;
                }
                this.imports = this.imports.slice(1);
                importItem.callback.apply(null, importItem.args);
            }
            if (this.variableImports.length === 0) {
                break;
            }
            var variableImport = this.variableImports[0];
            this.variableImports = this.variableImports.slice(1);
            variableImport();
        }
    } catch (e) {
    	this._currentDepth--;
    	throw e;
    }
    
    -- this._currentDepth;
    
    if (this._currentDepth === 0 && this._onSequencerEmpty) {
        this._onSequencerEmpty();
    }
};

// Entrypoint to be called from the Java code:
function lessCompile (input, variables) {
	var compileError, compileResult;
	
	var options = {
		processImports: true,
		compress: true,
		syncImport: true
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