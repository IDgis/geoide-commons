package controllers.toc;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;


public class TOC extends Controller{
	
	public Result buildTOC (final String mapId) {
		
		
	
		final ObjectNode result = Json.newObject ();
		return ok (result);
		
		
	}
}
