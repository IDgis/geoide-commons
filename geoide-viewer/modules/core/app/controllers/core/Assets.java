package controllers.core;

import javax.inject.Inject;

import play.api.mvc.Action;
import play.api.mvc.AnyContent;

public class Assets {

	private final controllers.Assets baseAssets;
	
	@Inject
	public Assets (final controllers.Assets baseAssets) {
		this.baseAssets = baseAssets;
	}
	
	public Action<AnyContent> at (final String path, final String file) {
		return baseAssets.at (path, file, false);
	}
}
