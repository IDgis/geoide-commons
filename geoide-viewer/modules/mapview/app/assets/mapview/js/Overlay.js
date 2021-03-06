define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/query',
	'dojo/dom-construct',
	'dojo/dom-class',
	'./Stateful',
	'dojo/Evented',
	'openlayers/ol'
], function (
	declare,
	lang,
	query,
	domConstruct,
	domClass,
	Stateful,
	Evented,
	ol
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
		
		// Add the "contains" method to SVG elements, OpenLayers uses this method
		// when handling mouse move events, but IE 9 doesn't provide it for SVG
		// elements:
		if (!elem.contains) {
			elem.contains = function () {
				return false;
			};
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
	
	return declare ([Stateful, Evented], {
		feature: null,
		width: 150,
		height: 100,
		offset: null,
		content: null,
		borderWidth: 2,
		arrowWidth: 10,
		arrowLength: 25,
		arrowDistance: 8,
		color: '#FF00FF',
		fillcolor: '#FF0000',
		selected: false,
		
		
		_handles: null,
		_geometryChangeKey: null,
		_container: null,
		_body: null,
		_svgRoot: null,
		_svgArrow: null,
		_svgBox: null,
		_olOverlay: null,
		
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
			
			// Create the DOM for this overlay and move the content into the container:
			this._container = document.createElement ('div');
			this._container.style.position = 'absolute';
			this._container.className = 'gi-overlay';
			
			this._svgRoot = svg ('svg', { }, this._container);
			
			this._body = document.createElement ('div');
			this._body.className = 'gi-overlay-body';
			this._body.style.position = 'absolute';
			this._container.appendChild (this._body);
			
			// Locate or create the content element:
			this._contentSetter (this.content);
			
			// Register watches:
			this.own (this.watch ('offset', lang.hitch (this, this.update)));
			this.own (this.watch ('width', lang.hitch (this, this.update)));
			this.own (this.watch ('height', lang.hitch (this, this.update)));
			this.own (this.watch ('borderWidth', lang.hitch (this, this.update)));
			this.own (this.watch ('arrowWidth', lang.hitch (this, this.update)));
			this.own (this.watch ('arrowLength', lang.hitch (this, this.update)));
			
			// Create an OpenLayers overlay:
			this._overlay = new ol.Overlay ({
				element: this._container,
				autoPan: true,
				autoPanAnimation: {
					duration: 250
				},
				position: this.feature.getGeometry ().getFirstCoordinate (),
				stopEvent: false,
				insertFirst: false
			});
			
			// Link the overlay to the feature:
			this.feature.set ('_geoideOverlay', this);
			this._geometryChangeKey = this.feature.on ('change', function (e) {
				this._overlay.setPosition (this.feature.getGeometry ().getFirstCoordinate ());
			}, this);
			
			var fstyle = this.feature.getStyle();
			if (fstyle instanceof Array) {
				fstyle = this.feature.getStyle()[0];
			}
			this.color = fstyle.getStroke().getColor(); 
			this.fillcolor = fstyle.getFill().getColor(); 
			
		},
		
		getProperties: function () {
			return {
				width: this.get ('width'),
				height: this.get ('height'),
				offset: this.get ('offset'),
				content: this.get ('content').innerHTML,
				borderWidth: this.get ('borderWidth'),
				arrowWidth: this.get ('arrowWidth'),
				arrowLength: this.get ('arrowLength'),
				arrowDistance: this.get ('arrowDistance')
			};
		},
		
		own: function (handle) {
			if (!this._handles) {
				this._handles = [ ];
			}
			
			this._handles.push (handle);
		},
		
		remove: function () {
			if (this._handles) {
				for (var i = 0; i < this._handles.length; ++ i) {
					this._handles[i].remove ();
				}
				this._handles = null;
			}
			
			domConstruct.destroy (this._container);
			
			this.feature.set ('_geoideOverlay', null);
			this.feature.unByKey (this._geometryChangeKey);
		},
		
		_contentSetter: function (content) {
			if (typeof content == 'string') {
				content = query (content)[0];
			} else if (content === null) {
				content = document.createElement ('div');
			}
			
			domConstruct.empty (this._body);
			this._body.appendChild (content);
			this.content = content;
		},
		
		_selectedSetter: function (selected) {
			domClass[selected ? 'add' : 'remove'] (this._container, 'selected');
			this.selected = selected;
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
				[dx * this.arrowDistance, dy * this.arrowDistance],
				[dx * (this.arrowLength + this.arrowDistance) + cx * (this.arrowWidth / 2), dy * (this.arrowLength + this.arrowDistance) + cy * (this.arrowWidth / 2)],
				[dx * (this.arrowLength + this.arrowDistance) - cx * (this.arrowWidth / 2), dy * (this.arrowLength + this.arrowDistance) - cy * (this.arrowWidth / 2)],
				[dx * this.arrowDistance, dy * this.arrowDistance]
			], [
				[dx * (this.arrowLength + this.arrowDistance), dy * (this.arrowLength + this.arrowDistance)],
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
					fill: this.color,
					stroke: this.color,
					'stroke-width': this.borderWidth
				}, this._svgRoot);
			}

			if (this._svgBox) {
				this._svgBox.setAttribute ('d', boxPath);
			} else {
				this._svgBox = svg ('path', {
					d: boxPath,
					fill: this.fillcolor,
					stroke: this.color,
					'stroke-width': this.borderWidth
				}, this._svgRoot);
			}
			
			var minX = Math.min (this.offset[0], 0) - this.borderWidth - (this.arrowWidth / 2),
				minY = Math.min (this.offset[1], 0) - this.borderWidth - (this.arrowWidth / 2),
				maxX = Math.max (this.offset[0] + this.width, this.arrowWidth / 2) + this.borderWidth + (this.arrowWidth / 2),
				maxY = Math.max (this.offset[1] + this.height, this.arrowWidth / 2) + this.borderWidth + (this.arrowWidth / 2);
			
			this._svgRoot.setAttribute ('viewBox', 
				'' + minX + ' ' + minY + ' ' + (maxX - minX) + ' ' + (maxY - minY) 
			);
			this._svgRoot.setAttribute ('width', maxX - minX);
			this._svgRoot.setAttribute ('height', maxY - minY);
			
			this._container.style.left = minX + 'px';
			this._container.style.top = minY + 'px';
			
			// Position the container body:
			this._body.style.left = (Math.max (0, this.offset[0]) + 1.5 * this.borderWidth + 0.5 * this.arrowWidth) + 'px';
			this._body.style.top = (Math.max (0, this.offset[1]) + 1.5 * this.borderWidth + 0.5 * this.arrowWidth) + 'px';
			this._body.style.width = (this.width - this.borderWidth) + 'px';
			this._body.style.height = (this.height - this.borderWidth) + 'px';
		},
		
		panIntoView: function (margin) {
			if (typeof margin === 'undefined') {
				margin = 0;
			}
			
			var map = this._overlay.getMap ();
			if (!map) {
				return;
			}
			
			var anchorPosition = map.getPixelFromCoordinate (this._overlay.getPosition ()),
				minX = anchorPosition[0] + this.offset[0] - this.borderWidth - (this.arrowWidth / 2) - margin,
				minY = anchorPosition[1] + this.offset[1] - this.borderWidth - (this.arrowWidth / 2) - margin,
				maxX = anchorPosition[0] + this.offset[0] + this.width + this.borderWidth + (this.arrowWidth / 2) + margin,
				maxY = anchorPosition[1] + this.offset[1] + this.height + this.borderWidth + (this.arrowWidth / 2) + margin;
			
			var mapSize = map.getSize (),
				mapRect = [0, 0, mapSize[0], mapSize[1]],
				boxRect = [minX, minY, maxX, maxY],
				offset = [0, 0];
			
			// min = map.getCoordinateFromPixel ([minX, minY]),
			// max = map.getCoordinateFromPixel ([maxX, maxY]);
			
			// Do nothing if the overlay is already contained in the map view:
			if (ol.extent.containsExtent (mapRect, boxRect)) {
				return;
			}
			
			for (var i = 0; i < 2; ++ i) {
				if (boxRect[i] < mapRect[i]) {
					// Move to the right:
					offset[i] = boxRect[i] - mapRect[i];
				} else if (boxRect[2 + i] > mapRect[2 + i]) {
					// Move to the left:
					offset[i] = boxRect[2 + i] - mapRect[2 + i];
				}
			}
			
			var mapView = map.getView (),
				center = mapView.getCenter (),
				centerPixel = map.getPixelFromCoordinate (center),
				modifiedCenter = map.getCoordinateFromPixel ([centerPixel[0] + offset[0], centerPixel[1] + offset[1]]);
			
			mapView.setCenter (modifiedCenter);
		}
	});
});