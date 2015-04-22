define ([
	'dojo/_base/declare',
	
	'./ViewerBase',
	'./ViewerQueryMixin',
	'./ViewerOverlayMixin'
], function (
	declare,
	
	ViewerBase,
	ViewerQueryMixin,
	ViewerOverlayMixin
) {
	
	return declare ([ViewerBase, ViewerQueryMixin, ViewerOverlayMixin], { });
});