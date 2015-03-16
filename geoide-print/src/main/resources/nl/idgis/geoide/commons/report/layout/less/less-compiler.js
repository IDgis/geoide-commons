// Define the "window" object and let it point to the global scope:
var window = this;

// Require js:
function require (a) { 
	return req.require (a); 
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

var less = require ('less')(/*null, [fs]*/);