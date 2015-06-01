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
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;
import com.winagile.gadget.MyException;
import com.winagile.gadget.ProjectChart;
import com.winagile.gadget.StaticParams;

/**
 * A resource of message.
 */
@Path("/projectchartmessage")
@AnonymousAllowed
@Produces({ "application/json" })
public class ProjectRestResource {

	final private CustomFieldManager customFM;
	final private IssueTypeManager itM;
	final private I18nResolver i18n;
	final private OptionsManager opM;
	final private ProjectChart barchart;
	private static final Logger log = LogManager
			.getLogger(ProjectRestResource.class);

	ProjectRestResource(CustomFieldManager customFM, IssueTypeManager itM,
			I18nResolver i18n, OptionsManager opM, ProjectChart barchart) {
		this.customFM = customFM;
		this.itM = itM;
		this.i18n = i18n;
		this.opM = opM;
		this.barchart = barchart;
	}

	@GET
	@Path("/generate")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMessage(@QueryParam("widthName") String width,
			@QueryParam("heightName") String height,
			@QueryParam("timeId") String timeId,
			@QueryParam("issuetypeId") String issuetypeId,
			@QueryParam("priorityId") String priorityId) {

		String Loginfo = "com.winagile.gadget.rest Generate: width:" + width
				+ ",height:" + height + ",timeId:" + timeId + ",issuetypeId:"
				+ issuetypeId + ",priorityId:" + priorityId;
		System.out.println(Loginfo);

		log.error(Loginfo);
		if (width != null && height != null && timeId != null
				&& issuetypeId != null && priorityId != null) {
			try {
				return Response
						.ok(barchart.generateChart(Integer.parseInt(width),
								Integer.parseInt(height),
								Long.parseLong(timeId), issuetypeId, priorityId))
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
	public Response validatePieChart(@QueryParam("timeId") String timeId,
			@QueryParam("issuetypeId") String issuetypeId,
			@QueryParam("priorityId") String priorityId) {
		String Loginfo = "com.winagile.gadget.rest Validate: timeId:" + timeId
				+ ",issuetypeId:" + issuetypeId + ",priorityId:" + priorityId;
		System.out.println(Loginfo);

		log.error(Loginfo);
		if (timeId != null && issuetypeId != null && priorityId != null) {
			try {
				barchart.generateChart(StaticParams.REPORT_IMAGE_WIDTH,
						StaticParams.REPORT_IMAGE_HEIGHT,
						Long.parseLong(timeId), issuetypeId, priorityId);
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
					.entity(getErrorCollection(new MyException("Field is null")))
					.build();
		}

	}

	private ErrorCollection getErrorCollection(MyException e) {
		return StaticParams.getErrorRep(e, log, "timeId");
	}

}