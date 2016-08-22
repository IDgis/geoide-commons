import com.fasterxml.jackson.databind.node.ObjectNode;

import play.http.HttpErrorHandler;
import play.mvc.*;
import play.mvc.Http.*;
import play.libs.F.*;
import play.libs.Json;

public class ErrorHandler implements HttpErrorHandler {
	
	@Override
	public Promise<Result> onClientError(RequestHeader request, int statusCode, java.lang.String message) {
		return Promise.pure(Results.badRequest(message));
	}

	@Override
    public Promise<Result> onServerError(RequestHeader request, Throwable e) {
		final ObjectNode result = Json.newObject ();
		result.put ("result", "failed");
		result.put ("message", e.getMessage ());
		return Promise.pure(Results.badRequest(result));
    }
}
