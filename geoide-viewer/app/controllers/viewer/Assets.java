package controllers.viewer;

import play.api.mvc.*;

public class Assets {
	private final controllers.Assets baseAssets;
	
	public Assets (final controllers.Assets baseAssets) {
		this.baseAssets = baseAssets;
		
	}
	public Action<AnyContent> at (final String path, final String file) {
		return baseAssets.at (path, file, false);
	}
}
