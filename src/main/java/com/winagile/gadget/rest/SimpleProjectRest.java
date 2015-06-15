package com.winagile.gadget.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.winagile.gadget.MyException;
import com.winagile.gadget.SimpleProjectChart;
import com.winagile.gadget.StaticParams;

@Path("/simpleprojectchartmessage")
@AnonymousAllowed
@Produces({ "application/json" })
public class SimpleProjectRest {

	final private SimpleProjectChart barchart;
	private static final Logger log = LogManager
			.getLogger(SimpleProjectRest.class);

	SimpleProjectRest(SimpleProjectChart barchart) {
		this.barchart = barchart;
	}

	@GET
	@Path("/generate")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMessage(@QueryParam("widthName") String width,
			@QueryParam("heightName") String height,
			@QueryParam("timeId") String timeId,
			@QueryParam("endtimeId") String endtimeId,
			@QueryParam("timeunitId") String timeunitId,
			@QueryParam("issuetypeId") String issuetypeId) {

		String Loginfo = "com.winagile.gadget.rest Generate: width:" + width
				+ ",height:" + height + ",timeId:" + timeId + ",issuetypeId:"
				+ issuetypeId + ",endtimeId:" + endtimeId;
		;
		System.out.println(Loginfo);

		log.error(Loginfo);
		if (width != null && height != null && timeId != null
				&& issuetypeId != null && endtimeId != null
				&& timeunitId != null) {
			try {
				return Response.ok(
						barchart.generateChart(Integer.parseInt(width),
								Integer.parseInt(height),
								Long.parseLong(timeId),
								Long.parseLong(endtimeId), issuetypeId,
								timeunitId)).build();
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
	public Response validatePieChart(@QueryParam("timeId") String timeId,
			@QueryParam("endtimeId") String endtimeId,
			@QueryParam("timeunitId") String timeunitId,
			@QueryParam("issuetypeId") String issuetypeId) {
		String Loginfo = "com.winagile.gadget.rest Validate: timeId:" + timeId
				+ ",issuetypeId:" + issuetypeId + ",endtimeId:" + endtimeId;
		;
		System.out.println(Loginfo);

		log.error(Loginfo);
		if (timeId != null && issuetypeId != null && endtimeId != null
				&& timeunitId != null) {
			try {
				barchart.generateChart(StaticParams.REPORT_IMAGE_WIDTH,
						StaticParams.REPORT_IMAGE_HEIGHT,
						Long.parseLong(timeId), Long.parseLong(endtimeId),
						issuetypeId, timeunitId);
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
		return StaticParams.getDoubleErrorRep(e, log, "timeId", "endtimeId");
	}

}