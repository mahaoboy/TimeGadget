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

import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.winagile.gadget.MyException;
import com.winagile.gadget.StaticParams;
import com.winagile.gadget.UnitIssueStatusChart;

@Path("/unitissuechartmessage")
@AnonymousAllowed
@Produces({ "application/json" })
public class UnitIssueStatusRest {

	final private UnitIssueStatusChart barchart;
	final private StatusManager sM;
	private static final Logger log = LogManager
			.getLogger(UnitIssueStatusRest.class);

	UnitIssueStatusRest(UnitIssueStatusChart barchart, StatusManager sM) {
		this.barchart = barchart;
		this.sM = sM;
	}

	@GET
	@Path("/generate")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMessage(@QueryParam("widthName") String width,
			@QueryParam("heightName") String height,
			@QueryParam("statusId") String statusId,
			@QueryParam("unitId") String unitId,
			@QueryParam("projectId") String projectId,
			@QueryParam("issuetypeId") String issuetypeId) {

		String Loginfo = "com.winagile.gadget.rest Generate: width:" + width
				+ ",height:" + height + ",statusId:" + statusId + ",unitId:"
				+ unitId + ",issuetypeId:" + issuetypeId;
		System.out.println(Loginfo);

		log.error(Loginfo);
		if (width != null && height != null && statusId != null
				&& unitId != null && issuetypeId != null && projectId != null) {
			try {
				return Response
						.ok(barchart.generateChart(Integer.parseInt(width),
								Integer.parseInt(height), statusId,
								Long.parseLong(unitId), issuetypeId, projectId))
						.build();
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
	public Response validatePieChart(@QueryParam("statusId") String statusId,
			@QueryParam("unitId") String unitId,
			@QueryParam("projectId") String projectId,
			@QueryParam("issuetypeId") String issuetypeId) {
		String Loginfo = "com.winagile.gadget.rest Validate: statusId:"
				+ statusId + ",unitId:" + unitId + ",issuetypeId:"
				+ issuetypeId;
		System.out.println(Loginfo);

		log.error(Loginfo);
		if (statusId != null && unitId != null && issuetypeId != null
				&& projectId != null) {
			try {
				barchart.generateChart(StaticParams.REPORT_IMAGE_WIDTH,
						StaticParams.REPORT_IMAGE_HEIGHT, statusId,
						Long.parseLong(unitId), issuetypeId, projectId);
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
		return StaticParams.getErrorRep(e, log, "unitId");
	}

	@GET
	@Path("statuslist")
	public Response getStatusList() {
		List<MyRestResourceModel> resList = new ArrayList<MyRestResourceModel>();
		for (Status st : sM.getStatuses()) {
			resList.add(new MyRestResourceModel(st.getId(), st
					.getNameTranslation()));
		}
		return Response.ok(resList).build();
	}
}