package i18n.printservice;

public interface ReportExceptions {
	String resourceNotFound (String resourceName);
	String unsupportedFormat (String inputFormat, String outputFormat);
	String printError ();
	String lessCompilation ();
	String genericError ();
}
