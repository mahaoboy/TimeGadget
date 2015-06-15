package com.winagile.gadget.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;
import com.winagile.gadget.BarChart;
import com.winagile.gadget.MyException;
import com.winagile.gadget.StaticParams;

/**
 * A resource of message.
 */
@Path("/timechartmessage")
@AnonymousAllowed
@Produces({ "application/json" })
public class MyRestResource {

	final private CustomFieldManager customFM;
	final private IssueTypeManager itM;
	final private I18nResolver i18n;
	final private OptionsManager opM;
	final private BarChart barchart;
	final private ProjectManager pm;
	private static final Logger log = LogManager
			.getLogger(MyRestResource.class);

	MyRestResource(CustomFieldManager customFM, IssueTypeManager itM,
			I18nResolver i18n, OptionsManager opM, BarChart barchart,
			ProjectManager pm) {
		this.customFM = customFM;
		this.itM = itM;
		this.i18n = i18n;
		this.opM = opM;
		this.barchart = barchart;
		this.pm = pm;
	}

	@GET
	@Path("/generate")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMessage(@QueryParam("widthName") String width,
			@QueryParam("heightName") String height,
			@QueryParam("unitId") String unitId,
			@QueryParam("timeId") String timeId,
			@QueryParam("endtimeId") String endtimeId,
			@QueryParam("issuetypeId") String issuetypeId,
			@QueryParam("timeunitId") String timeunitId,
			@QueryParam("projectId") String projectId,
			@QueryParam("priorityId") String priorityId) {

		String Loginfo = "com.winagile.gadget.rest Generate: width:" + width
				+ ",height:" + height + ",unitId:" + unitId + ",timeId:"
				+ timeId + ",issuetypeId:" + issuetypeId + ",priorityId:"
				+ priorityId + ",endtimeId:" + endtimeId;
		System.out.println(Loginfo);

		log.error(Loginfo);
		if (width != null && height != null && unitId != null && timeId != null
				&& issuetypeId != null && priorityId != null
				&& endtimeId != null && timeunitId != null && projectId != null) {
			try {
				return Response.ok(
						barchart.generateChart(Integer.parseInt(width),
								Integer.parseInt(height),
								Long.parseLong(timeId),
								Long.parseLong(endtimeId),
								Long.parseLong(unitId), issuetypeId,
								priorityId, timeunitId, projectId)).build();
			} catch (MyException e) {
				return Response.status(400)
						.entity(getErrorCollection(new MyException(e))).build();
			} catch (Exception e) {
				return Response.status(400)
						.entity(getErrorCollection(new MyException(e))).build();
			}
		}
		return Response
				.status(400)
				.entity(getErrorCollection(new MyException(
						"demogadget.exception.fieldnullerror"))).build();

	}

	@GET
	@Path("validate")
	public Response validatePieChart(@QueryParam("unitId") String unitId,
			@QueryParam("timeId") String timeId,
			@QueryParam("endtimeId") String endtimeId,
			@QueryParam("issuetypeId") String issuetypeId,
			@QueryParam("timeunitId") String timeunitId,
			@QueryParam("projectId") String projectId,
			@QueryParam("priorityId") String priorityId) {
		String Loginfo = "com.winagile.gadget.rest Validate: unitId:" + unitId
				+ ",timeId:" + timeId + ",issuetypeId:" + issuetypeId
				+ ",priorityId:" + priorityId + ",endtimeId:" + endtimeId;
		System.out.println(Loginfo);

		log.error(Loginfo);
		if (unitId != null && timeId != null && issuetypeId != null
				&& priorityId != null && endtimeId != null
				&& timeunitId != null && projectId != null) {
			try {
				barchart.generateChart(StaticParams.REPORT_IMAGE_WIDTH,
						StaticParams.REPORT_IMAGE_HEIGHT,
						Long.parseLong(timeId), Long.parseLong(endtimeId),
						Long.parseLong(unitId), issuetypeId, priorityId,
						timeunitId, projectId);
				return Response.ok(
						new String("No input validation errors found."))
						.build();
			} catch (MyException e) {
				return Response.status(400).entity(getErrorCollection(e))
						.build();
			} catch (Exception e) {
				return Response.status(400)
						.entity(getErrorCollection(new MyException(e))).build();
			}
		} else {
			return Response
					.status(400)
					.entity(getErrorCollection(new MyException(
							"demogadget.exception.fieldnullerror"))).build();
		}

	}

	private ErrorCollection getErrorCollection(MyException e) {
		if (e.getExType() != null && e.getExType().equals("time")) {
			return StaticParams
					.getDoubleErrorRep(e, log, "timeId", "endtimeId");
		} else {
			return StaticParams.getErrorRep(e, log, "unitId");
		}
	}

	@GET
	@Path("customfieldlist")
	public Response getCustomFieldList() {
		List<MyRestResourceModel> resList = new ArrayList<MyRestResourceModel>();

		resList.add(new MyRestResourceModel(String
				.valueOf(StaticParams.createdValue), i18n
				.getText(StaticParams.createdName)));
		resList.add(new MyRestResourceModel(String
				.valueOf(StaticParams.updatedValue), i18n
				.getText(StaticParams.updatedName)));
		resList.add(new MyRestResourceModel(String
				.valueOf(StaticParams.solvedValue), i18n
				.getText(StaticParams.solvedName)));
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
			resList.add(new MyRestResourceModel(it.getId(), it
					.getNameTranslation()));
		}
		return Response.ok(resList).build();
	}

	@GET
	@Path("priolist")
	public Response getPrioList() {
		List<MyGroupResourceModel> groupList = new ArrayList<MyGroupResourceModel>();

		List<MyRestResourceModel> resList = new ArrayList<MyRestResourceModel>();
		resList.add(new MyRestResourceModel(StaticParams.priorityValue
				+ StaticParams.delimeterV + StaticParams.priorityitemHE, i18n
				.getText(StaticParams.priorityName)
				+ StaticParams.delimeter
				+ i18n.getText(StaticParams.priorityNameHE)));
		resList.add(new MyRestResourceModel(StaticParams.priorityValue
				+ StaticParams.delimeterV + StaticParams.priorityitemH, i18n
				.getText(StaticParams.priorityName)
				+ StaticParams.delimeter
				+ i18n.getText(StaticParams.priorityNameH)));
		resList.add(new MyRestResourceModel(StaticParams.priorityValue
				+ StaticParams.delimeterV + StaticParams.priorityitemM, i18n
				.getText(StaticParams.priorityName)
				+ StaticParams.delimeter
				+ i18n.getText(StaticParams.priorityNameM)));
		resList.add(new MyRestResourceModel(StaticParams.priorityValue
				+ StaticParams.delimeterV + StaticParams.priorityitemL, i18n
				.getText(StaticParams.priorityName)
				+ StaticParams.delimeter
				+ i18n.getText(StaticParams.priorityNameL)));
		resList.add(new MyRestResourceModel(StaticParams.priorityValue
				+ StaticParams.delimeterV + StaticParams.priorityitemLE, i18n
				.getText(StaticParams.priorityName)
				+ StaticParams.delimeter
				+ i18n.getText(StaticParams.priorityNameLE)));

		groupList.add(new MyGroupResourceModel(new MyGroupMiddleResourceModel(
				resList, i18n.getText(StaticParams.priorityName))));

		for (CustomField cf : customFM.getCustomFieldObjects()) {
			if (cf.getCustomFieldType().getKey().equals(StaticParams.selectKey)) {
				resList = new ArrayList<MyRestResourceModel>();
				for (Option cfo : opM.getOptions(cf.getConfigurationSchemes()
						.listIterator().next().getOneAndOnlyConfig())) {
					resList.add(new MyRestResourceModel(cf.getIdAsLong()
							.toString()
							+ StaticParams.delimeterV
							+ cfo.getOptionId(), cf.getName()
							+ StaticParams.delimeter + cfo.getValue()));
				}
				groupList.add(new MyGroupResourceModel(
						new MyGroupMiddleResourceModel(resList, cf.getName())));
			}
		}

		return Response.ok(groupList).build();

	}

	@GET
	@Path("projectlist")
	public Response getProjectList() {
		List<MyRestResourceModel> resList = new ArrayList<MyRestResourceModel>();

		resList.add(new MyRestResourceModel(String
				.valueOf(StaticParams.allProject), i18n
				.getText(StaticParams.allProjectName)));
		for (Project ip : pm.getProjectObjects()) {
			resList.add(new MyRestResourceModel(ip.getId().toString(), ip
					.getName()));
		}
		return Response.ok(resList).build();
	}
}