define ([
	'dojo/_base/declare',
	
	'./ViewerBase',
	'./ViewerQueryMixin'
], function (
	declare,
	
	ViewerBase,
	ViewerQueryMixin
) {
	
	return declare ([ViewerBase, ViewerQueryMixin], { });
});