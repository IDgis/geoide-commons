define ([
	'dojo/_base/declare',
	'dojo/query',
	'./Stateful'
], function (
	declare,
	query,
	Stateful
) {
	
	function svg (name, attributes, parent) {
		var elem = document.createElementNS ('http://www.w3.org/2000/svg', name);
		
		if (attributes) {
			for (var i in attributes) {
				if (!attributes.hasOwnProperty (i)) {
					continue;
				}
				
				elem.setAttribute (i, attributes[i]);
			}
		}
		
		if (parent) {
			parent.appendChild (elem);
		}
		
		return elem;
	}
	
	function path () {
		var p = '';
		
		for (var i = 0; i < arguments.length; ++ i) {
			var coords = arguments[i];
			
			if (coords.length === 0) {
				continue;
			}
			
			p += 'M ' + coords[0][0] + ' ' + coords[0][1] + ' ';
			
			for (var j = 1; j < coords.length; ++ j) {
				p += 'L ' + coords[j][0] + ' ' + coords[j][1] + ' ';
			}
		}
		
		return p;
	}
	
	return declare ([], {
		width: 150,
		height: 100,
		offset: null,
		content: null,
		borderWidth: 2,
		arrowWidth: 20,
		arrowLength: 20,
		
		_container: null,
		_body: null,
		_svgRoot: null,
		_svgArrow: null,
		_svgBox: null,
		
		constructor: function (options) {
			this.offset = [-50, 50];
			
			// Copy settings:
			if (options) {
				for (var i in options) {
					if (options.hasOwnProperty (i)) {
						this[i] = options[i];
					}
				}
			}
			
			// Locate or create the content element:
			if (typeof this.content == 'string') {
				this.content = query (this.content)[0];
			} else if (this.content === null) {
				this.content = document.createElement ('div');
			}
			
			// Create the DOM for this overlay and move the content into the container:
			this._container = document.createElement ('div');
			this._container.style.position = 'absolute';
			
			this._svgRoot = svg ('svg', { }, this._container);
			
			this._body = document.createElement ('div');
			this._body.className = 'gi-overlay-body';
			this._body.style.position = 'absolute';
			this._container.appendChild (this._body);
			this._body.appendChild (this.content);
		},
		
		update: function () {
			var center = [this.offset[0] + (this.width / 2), this.offset[1] + (this.height / 2)],
				dx = center[0],
				dy = center[1],
				length = Math.sqrt (dx * dx + dy * dy);
			
			dx /= length;
			dy /= length;
			
			var cx = dy,
				cy = -dx;
			
			var arrowPath = path ([
				[0, 0],
				[dx * this.arrowLength + cx * (this.arrowWidth / 2), dy * this.arrowLength + cy * (this.arrowWidth / 2)],
				[dx * this.arrowLength - cx * (this.arrowWidth / 2), dy * this.arrowLength - cy * (this.arrowWidth / 2)],
				[0, 0]
			], [
				[dx * this.arrowWidth, dy * this.arrowWidth],
				[center[0], center[1]]
			]);
			
			var boxPath = path ([
				[this.offset[0], this.offset[1]],
				[this.offset[0], this.offset[1] + this.height],
				[this.offset[0] + this.width, this.offset[1] + this.height],
				[this.offset[0] + this.width, this.offset[1]],
				[this.offset[0], this.offset[1]]
			]);
			
			if (this._svgArrow) {
				this._svgArrow.setAttribute ('d', arrowPath);
			} else {
				this._svgArrow = svg ('path', {
					d: arrowPath,
					fill: 'white',
					stroke: 'black',
					'stroke-width': this.borderWidth
				}, this._svgRoot);
			}

			if (this._svgBox) {
				this._svgBox.setAttribute ('d', boxPath);
			} else {
				this._svgBox = svg ('path', {
					d: boxPath,
					fill: 'white',
					stroke: 'black',
					'stroke-width': this.borderWidth
				}, this._svgRoot);
			}
			
			var minX = Math.min (this.offset[0], 0) - this.borderWidth,
				minY = Math.min (this.offset[1], 0) - this.borderWidth,
				maxX = Math.max (this.offset[0] + this.width, 0) + this.borderWidth,
				maxY = Math.max (this.offset[1] + this.height, 0) + this.borderWidth;
			
			this._svgRoot.setAttribute ('viewBox', 
				'' + minX + ' ' + minY + ' ' + (maxX - minX) + ' ' + (maxY - minY) 
			);
			this._svgRoot.setAttribute ('width', maxX - minX);
			this._svgRoot.setAttribute ('height', maxY - minY);
			
			this._container.style.left = minX + 'px';
			this._container.style.top = minY + 'px';
			
			// Position the container body:
			this._body.style.left = (Math.max (0, this.offset[0]) + 1.5 * this.borderWidth) + 'px';
			this._body.style.top = (Math.max (0, this.offset[1]) + 1.5 * this.borderWidth) + 'px';
			this._body.style.width = (this.width - this.borderWidth) + 'px';
			this._body.style.height = (this.height - this.borderWidth) + 'px';
		}
	});
});