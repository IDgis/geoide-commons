package controllers.mapview;

import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

public class Symbol extends Controller {
	
	public static Promise<Result> legendSymbol (final String serviceLayerId) {
		return Promise.pure ((Result) ok ());
	}
}