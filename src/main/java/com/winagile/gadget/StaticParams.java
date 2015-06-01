package com.winagile.gadget;

import java.io.PrintWriter;
import org.apache.log4j.Logger;
import java.io.StringWriter;

import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

public class StaticParams {
	final public static int REPORT_IMAGE_WIDTH = 600;
	final public static int REPORT_IMAGE_HEIGHT = 600;

	final public static String selectKey = "com.atlassian.jira.plugin.system.customfieldtypes:select";
	final public static String priorityValue = "priority";
	final public static String project = "project";
	final public static String issuetype = "issuetype";
	final public static String issuestatus = "status";
	final public static String priorityName = "gadget.winagle.config.prio";
	final public static String delimeter = " -> ";
	final public static String delimeterV = "-";
	final public static String priorityitemHE = "Highest";
	final public static String priorityNameHE = "gadget.winagle.config.prioHE";
	final public static String priorityitemH = "High";
	final public static String priorityNameH = "gadget.winagle.config.prioH";
	final public static String priorityitemM = "Medium";
	final public static String priorityNameM = "gadget.winagle.config.prioM";
	final public static String priorityitemL = "Low";
	final public static String priorityNameL = "gadget.winagle.config.prioL";
	final public static String priorityitemLE = "Lowest";
	final public static String priorityNameLE = "gadget.winagle.config.prioLE";

	final public static String equalD = " = ";
	final public static String unequalD = " != ";
	final public static String andD = " and ";
	final public static String quoteD = "\"";

	final public static String critical = "Critical";
	final public static String normal = "Normal";

	public static String getPrintStack(Throwable e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

	public static ErrorCollection getErrorRep(MyException e, Logger log,
			String field) {
		e.printStackTrace();

		log.error(StaticParams.getPrintStack(e));
		SimpleErrorCollection sec = new SimpleErrorCollection();
		sec.addError("field", field);

		sec.addError("error", "demogadget.fielderror");
		ErrorCollection ec = new ErrorCollection();
		ec.addErrorMessage(StaticParams.getPrintStack(e));
		ec.addErrorCollection(sec);

		return ec;
	}
}
