package controllers.viewer;

import play.api.mvc.*;

public class Assets {
	public static Action<AnyContent> at (final String path, final String file) {
		return controllers.Assets.at (path, file, false);
	}

}
