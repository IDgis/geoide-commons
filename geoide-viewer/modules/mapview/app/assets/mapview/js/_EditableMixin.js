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
		text: null,
		
		_editorNode: null,
		
		constructor: function () {
			domClass.add (this._body, 'moveable');
			
			on (this._body, 'mousedown', lang.hitch (this, function (e) {
				e.stopPropagation ();
				
				if (e.target.tagName.toLowerCase () == 'textarea') {
					return;
				}
				
				e.preventDefault ();
				
				this._dragMoveable (e);
			}));
			
			if (this.text) {
				this.set ('text', this.text);
			}
		},
		
		_textSetter: function (text) {
			var newContent = document.createElement ('div');
			newContent.appendChild (document.createTextNode (text));
			newContent.className = 'editable-text';
			
			this.set ('content', newContent);
			this.text = text;
		},
		
		_dragMoveable: function (e) {
			var startX = e.clientX,
				startY = e.clientY,
				startOffset = this.get ('offset'),
				handles = [ ],
				self = this;
			
			var updateDrag = function (dx, dy) {
				if (Math.abs (dx) < 5 && Math.abs (dy) < 5) {
					return;
				}
				
				var newOffset = [startOffset[0] + dx, startOffset[1] + dy];
				
				self.set ('offset', newOffset);
			};
			
			handles.push (on (window, 'mousemove', function (e) {
				updateDrag (e.clientX - startX, e.clientY - startY);
			}));
			
			handles.push (on (window, 'mouseup', function (e) {
				var dx = e.clientX - startX,
					dy = e.clientY - startY;
				
				updateDrag (dx, dy);
				
				for (var i = 0; i < handles.length; ++ i) {
					handles[i].remove ();
				}
				
				if (Math.abs (dx) < 5 && Math.abs (dy) < 5) {
					self.edit ();
				}
			}));
		},
		
		edit: function () {
			if (this._editorNode) {
				return;
			}
			
			this._editorNode = document.createElement ('textarea');
			this.set ('content', this._editorNode);
			
			if (this.text) {
				this.content.appendChild (document.createTextNode (this.text));
			}
			
			this.content.focus ();
			this.content.style.position = 'absolute';
			this.content.style.left = '0px';
			this.content.style.top = '0px';
			this.content.style.width = '100%';
			this.content.style.height = '100%';
			this.content.style.resize = 'none';
			
			var blurHandle = on (this.content, 'blur', lang.hitch (this, function (e) {
				blurHandle.remove ();
				
				this.set ('text', this.content.value);
				this._editorNode = null;
				
				// Update the height of the box:
				var newHeight = Math.max (32, this.content.offsetHeight + this.borderWidth * 2);
				this.set ('height', newHeight);
			}));
		}
	});
});