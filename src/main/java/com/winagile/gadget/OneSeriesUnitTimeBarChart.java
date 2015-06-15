package com.winagile.gadget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;

/**
 * A simple demonstration application showing how to create a bar chart.
 * 
 */
public class OneSeriesUnitTimeBarChart {

	final private IssueTypeManager itM;
	final private CustomFieldManager customFM;
	final private SearchService ss;
	final private OptionsManager opM;
	private Long Aid;
	private Long Cid;
	private CustomField Bcf;
	private String issueType;
	private User auser;
	private String projectId;
	final private ProjectManager pm;

	private String timeunitId;
	final private PluginLicenseManager licenseManager;
	final private I18nResolver i18n;

	private static final Logger log = LogManager
			.getLogger(OneSeriesUnitTimeBarChart.class);

	OneSeriesUnitTimeBarChart(CustomFieldManager customFM,
			IssueTypeManager itM, SearchService ss, OptionsManager opM,
			PluginLicenseManager licenseManager, I18nResolver i18n,
			ProjectManager pm) {
		this.customFM = customFM;
		this.itM = itM;
		this.ss = ss;
		this.opM = opM;
		this.licenseManager = licenseManager;
		this.i18n = i18n;
		this.pm = pm;
	}

	public StaticParams.PieChart generateChart(int width, int height, Long Aid,
			Long Cid, Long Bid, String issueType, String timeunitId,
			String projectId) throws MyException {

		StaticParams.checkLicenseStatus(licenseManager, i18n);
		this.issueType = issueType;
		this.Aid = Aid;
		this.Cid = Cid;
		this.Bcf = customFM.getCustomFieldObject(Bid);
		this.timeunitId = timeunitId;
		this.projectId = projectId;
		final CategoryDataset categoryDataset = createDataset();
		JFreeChart jchart = StaticParams.createChart(categoryDataset);
		return StaticParams
				.createPieChart(width, height, jchart, categoryDataset,
						projectId.equals(StaticParams.allProject) ? "" : pm
								.getProjectObj(Long.parseLong(projectId))
								.getName());
	}

	private CategoryDataset createDataset() throws MyException {
		final String series1 = "First";
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Options options;
		try {
			options = opM.getOptions(Bcf.getConfigurationSchemes()
					.listIterator().next().getOneAndOnlyConfig());
			if (options.isEmpty()) {
				throw new Exception();
			}
		} catch (Exception e3) {
			e3.printStackTrace();
			log.error(StaticParams.getPrintStack(e3));
			throw new MyException("unit", "demogadget.fielderror");
		}

		for (Option op : options.getRootOptions()) {
			getDataSet(dataset, op, series1);
		}
		return dataset;

	}

	private void getDataSet(DefaultCategoryDataset dataset, Option op,
			String series) throws MyException {
		auser = ApplicationUsers.toDirectoryUser(ComponentAccessor
				.getJiraAuthenticationContext().getUser());

		Map<String, Double> timeMap = new HashMap<String, Double>();
		timeMap.put(StaticParams.totalTime, 0d);
		timeMap.put(StaticParams.totalNum, 0d);
		String jqlQuery;
		SearchService.ParseResult parseResult;
		try {

			jqlQuery = "\"" + Bcf.getFieldName() + "\" = \"" + op.getValue()
					+ "\" and issuetype = "
					+ itM.getIssueType(issueType).getName();
			log.error("projectIdprojectId projectIdprojectId projectIdprojectId:"+projectId);
			if (!projectId.equals(StaticParams.allProject)) {
				jqlQuery += StaticParams.andD + StaticParams.project
						+ StaticParams.equalD + StaticParams.quoteD
						+ pm.getProjectObj(Long.parseLong(projectId)).getName()
						+ StaticParams.quoteD;
			}
			final String logInfo = "com.winagile.demo.jira.reports Info: JQL : "
					+ jqlQuery;
			System.out.println(logInfo);
			log.error(logInfo);
			parseResult = ss.parseQuery(auser, jqlQuery);
		} catch (Exception e2) {
			e2.printStackTrace();
			log.error(StaticParams.getPrintStack(e2));
			throw new MyException("unit", "demogadget.fielderror");
		}

		if (parseResult.isValid()) {
			Query query = parseResult.getQuery();
			SearchResults results;
			try {
				results = ss.search(auser, query,
						PagerFilter.getUnlimitedFilter());
				List<Issue> issues = results.getIssues();
				for (Issue issue : issues) {
					StaticParams.getTotalTime(timeMap, issue, Aid, Cid,
							customFM, timeunitId);

				}
			} catch (SearchException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();

				log.error(StaticParams.getPrintStack(e1));
				throw new MyException("unit", "demogadget.fielderror");
			} catch (Exception e2) {
				e2.printStackTrace();

				log.error(StaticParams.getPrintStack(e2));
				throw new MyException("time", "demogadget.timefielderror");
			}

			dataset.addValue(
					timeMap.get(StaticParams.totalTime)
							/ timeMap.get(StaticParams.totalNum), series,
					op.getValue());
		} else {
			System.out
					.println("com.winagile.demo.jira.reports ERROR: JQL is not valid");
		}
	}

}
