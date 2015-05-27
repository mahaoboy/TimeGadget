package com.winagile.gadget.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;
import com.winagile.gadget.BarChart;
import com.winagile.gadget.MyException;

/**
 * A resource of message.
 */
@Path("/message")
@AnonymousAllowed
@Produces({ "application/json" })
public class MyRestResource {

	final private static int REPORT_IMAGE_WIDTH = 600;
	final private static int REPORT_IMAGE_HEIGHT = 600;
	final private CustomFieldManager customFM;
	final private IssueTypeManager itM;
	final private I18nResolver i18n;
	final private OptionsManager opM;

	final private static String selectKey = "com.atlassian.jira.plugin.system.customfieldtypes:select";
	final private static String priorityValue = "priority";
	final private static String priorityName = "gadget.winagle.config.prio";
	final private static String delimeter = " -> ";
	final private static String delimeterV = ":";
	final private static String priorityitemHE = "Highest";
	final private static String priorityNameHE = "gadget.winagle.config.prioHE";
	final private static String priorityitemH = "High";
	final private static String priorityNameH = "gadget.winagle.config.prioH";
	final private static String priorityitemM = "Medium";
	final private static String priorityNameM = "gadget.winagle.config.prioM";
	final private static String priorityitemL = "Low";
	final private static String priorityNameL = "gadget.winagle.config.prioL";
	final private static String priorityitemLE = "Lowest";
	final private static String priorityNameLE = "gadget.winagle.config.prioLE";

	MyRestResource(CustomFieldManager customFM, IssueTypeManager itM,
			I18nResolver i18n, OptionsManager opM) {
		this.customFM = customFM;
		this.itM = itM;
		this.i18n = i18n;
		this.opM = opM;
	}

	@GET
	@Path("/generate")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMessage(@QueryParam("widthName") String width,
			@QueryParam("heightName") String height,
			@QueryParam("unitId") String unitId,
			@QueryParam("timeId") String timeId) {
		System.out.println("com.winagile.gadget.rest : width:" + width
				+ ",height:" + height + ",unitId:" + unitId + ",timeId:"
				+ timeId);
		if (width != null && height != null && unitId != null && timeId != null) {
			try {
				return Response
						.ok(new BarChart().generateChart(
								Integer.parseInt(width),
								Integer.parseInt(height),
								Long.parseLong(timeId), Long.parseLong(unitId)))
						.build();
			} catch (MyException e) {
				return Response.status(400)
						.entity(getErrorCollection(new MyException(e))).build();
			} catch (Exception e) {
				return Response.status(400)
						.entity(getErrorCollection(new MyException(e))).build();
			}
		}
		return Response.status(400)
				.entity(getErrorCollection(new MyException())).build();

	}

	@GET
	@Path("validate")
	public Response validatePieChart(@QueryParam("unitId") String unitId,
			@QueryParam("timeId") String timeId) {
		try {
			new BarChart().generateChart(REPORT_IMAGE_WIDTH,
					REPORT_IMAGE_HEIGHT, Long.parseLong(timeId),
					Long.parseLong(unitId));
			return Response.ok(new String("No input validation errors found."))
					.build();
		} catch (MyException e) {
			return Response.status(400).entity(getErrorCollection(e)).build();
		} catch (Exception e) {
			return Response.status(400)
					.entity(getErrorCollection(new MyException(e))).build();
		}

	}

	private ErrorCollection getErrorCollection(MyException e) {
		e.printStackTrace();
		SimpleErrorCollection sec = new SimpleErrorCollection();
		if (e.getExType() != null && e.getExType().equals("time")) {
			sec.addError("field", "timeId");
		} else {
			sec.addError("field", "unitId");
		}
		sec.addError("error", "gadget.winagile.field.error");
		ErrorCollection ec = new ErrorCollection();
		ec.addErrorMessage("gadget.winagile.field.error");
		ec.addErrorCollection(sec);

		return ec;
	}

	@GET
	@Path("customfieldlist")
	public Response getCustomFieldList() {
		List<MyRestResourceModel> resList = new ArrayList<MyRestResourceModel>();
		for (CustomField cf : customFM.getCustomFieldObjects()) {
			resList.add(new MyRestResourceModel(cf.getIdAsLong().toString(), cf
					.getName()));
		}
		return Response.ok(resList).build();
	}

	@GET
	@Path("issuetypelist")
	public Response getIssueTypeList() {
		List<MyRestResourceModel> resList = new ArrayList<MyRestResourceModel>();
		for (IssueType it : itM.getIssueTypes()) {
			resList.add(new MyRestResourceModel(it.getId(), it.getName()));
		}
		return Response.ok(resList).build();
	}

	@GET
	@Path("priolist")
	public Response getPrioList() {
		List<MyRestResourceModel> resList = new ArrayList<MyRestResourceModel>();
		resList.add(new MyRestResourceModel(priorityValue + delimeterV
				+ priorityitemHE, i18n.getText(priorityName) + delimeter
				+ i18n.getText(priorityNameHE)));
		resList.add(new MyRestResourceModel(priorityValue + delimeterV
				+ priorityitemH, i18n.getText(priorityName) + delimeter
				+ i18n.getText(priorityNameH)));
		resList.add(new MyRestResourceModel(priorityValue + delimeterV
				+ priorityitemM, i18n.getText(priorityName) + delimeter
				+ i18n.getText(priorityNameM)));
		resList.add(new MyRestResourceModel(priorityValue + delimeterV
				+ priorityitemL, i18n.getText(priorityName) + delimeter
				+ i18n.getText(priorityNameL)));
		resList.add(new MyRestResourceModel(priorityValue + delimeterV
				+ priorityitemLE, i18n.getText(priorityName) + delimeter
				+ i18n.getText(priorityNameLE)));
		for (CustomField cf : customFM.getCustomFieldObjects()) {
			if (cf.getCustomFieldType().getKey().equals(selectKey)) {
				for (Option cfo : opM.getOptions(cf.getConfigurationSchemes()
						.listIterator().next().getOneAndOnlyConfig())) {
					resList.add(new MyRestResourceModel(cf.getIdAsLong()
							.toString() + delimeterV + cfo.getOptionId(), cf
							.getName() + delimeter + cfo.getValue()));
				}

			}
		}

		return Response.ok(resList).build();

	}
}