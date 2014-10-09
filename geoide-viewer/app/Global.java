import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import play.Application;
import play.GlobalSettings;
import viewer.config.ViewerConfig;

public class Global extends GlobalSettings {
	
	private AnnotationConfigApplicationContext applicationContext;

	@Override
	public void onStart (final Application application) {
		applicationContext = new AnnotationConfigApplicationContext ();
		
		applicationContext.register (ViewerConfig.class);
		
		applicationContext.refresh ();
	}
	
	@Override
	public void onStop (final Application application) {
		applicationContext.close ();
	}

	@Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
		return applicationContext.getBean (controllerClass);
    }
}
