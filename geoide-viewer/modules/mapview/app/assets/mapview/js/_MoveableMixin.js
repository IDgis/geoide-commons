define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	
	'dojo/dom-class',
	'dojo/on'
], function (
	declare,
	lang,
	domClass,
	on
) {
	return declare ([], {
		constructor: function () {
			domClass.add (this._body, 'moveable');
			
			on (this._body, 'mousedown', lang.hitch (this, function (e) {
				e.preventDefault ();
				e.stopPropagation ();
				
				this._dragMoveable (e);
			}));
		},
		
		_dragMoveable: function (e) {
			console.log ('Drag: ', e);
			
			var startX = e.clientX,
				startY = e.clientY,
				startOffset = this.get ('offset'),
				handles = [ ],
				self = this;
			
			var updateDrag = function (dx, dy) {
				var newOffset = [startOffset[0] + dx, startOffset[1] + dy];
				
				self.set ('offset', newOffset);
			};
			
			handles.push (on (window, 'mousemove', function (e) {
				updateDrag (e.clientX - startX, e.clientY - startY);
			}));
			
			handles.push (on (window, 'mouseup', function (e) {
				updateDrag (e.clientX - startX, e.clientY - startY);
				
				for (var i = 0; i < handles.length; ++ i) {
					handles[i].remove ();
				}
			}));
		}
	});
});