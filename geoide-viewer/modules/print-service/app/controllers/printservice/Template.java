package controllers.printservice;

import nl.idgis.geoide.documentcache.service.FileStore;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

public class Template extends Controller {
	private final FileStore fileStore;
	
	public Template(FileStore fileStore) {
		this.fileStore = fileStore;
		
		if (fileStore == null) {
			throw new NullPointerException ("fileStore cannot be null");
		}
		
	}
		
	public	Result getTemplateFiles () throws Throwable {
		String[] files = fileStore.getFileList();
		String list = "";
		for(int n = 0; n < files.length; n++){
			list +=  files[n] + ";";
		}
		return ok(list);
	}
	
	public Result getTemplateProperties (final String templateName) throws Throwable {
		return ok();
	
	}
}
