
define ([

'dojo/_base/declare',

'dojo/dom', 

'dojo/Deferred',


'dojo/domReady!'
], 

function(
declare,

dom,
 
Deferred) 
{
	declare("DOMBehaviour", null , {
		constructor: function(node){
			this.node = node;
		},
		startup: function () {
			this.startUpPromise = new Deferred ();
			
			return startUpPromise;
		}
	}		
	
});