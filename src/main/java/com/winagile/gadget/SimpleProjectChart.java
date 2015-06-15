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
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;

public class SimpleProjectChart {
	final private IssueTypeManager itM;
	final private CustomFieldManager customFM;
	final private SearchService ss;
	final private ProjectManager pM;
	private Long Aid;
	private Long Cid;
	private String issueType;
	private User auser;
	private String timeunitId;
	final private PluginLicenseManager licenseManager;

	final private I18nResolver i18n;

	private static final Logger log = LogManager
			.getLogger(SimpleProjectChart.class);

	SimpleProjectChart(CustomFieldManager customFM, IssueTypeManager itM,
			SearchService ss, ProjectManager pM,
			PluginLicenseManager licenseManager, I18nResolver i18n) {
		this.customFM = customFM;
		this.itM = itM;
		this.ss = ss;
		this.pM = pM;
		this.i18n = i18n;
		this.licenseManager = licenseManager;
	}

	public StaticParams.PieChart generateChart(int width, int height, Long Aid,
			Long Cid, String issueType, String timeunitId) throws MyException {

		StaticParams.checkLicenseStatus(licenseManager, i18n);
		this.issueType = issueType;
		this.Aid = Aid;
		this.Cid = Cid;
		this.timeunitId = timeunitId;
		final CategoryDataset categoryDataset = createDataset(Aid, Cid);
		JFreeChart jchart = StaticParams.createChart(categoryDataset);

		return StaticParams.createPieChart(width, height, jchart,
				categoryDataset, "");
	}

	private CategoryDataset createDataset(Long Aid, Long Cid)
			throws MyException {
		final String series1 = "First";
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		List<Project> pl = pM.getProjectObjects();

		for (Project proj : pl) {
			getDataSet(dataset, proj, series1);
		}
		return dataset;

	}

	private void getDataSet(DefaultCategoryDataset dataset, Project proj,
			String series) throws MyException {
		auser = ApplicationUsers.toDirectoryUser(ComponentAccessor
				.getJiraAuthenticationContext().getUser());

		Map<String, Double> timeMap = new HashMap<String, Double>();
		timeMap.put(StaticParams.totalTime, 0d);
		timeMap.put(StaticParams.totalNum, 0d);
		String jqlQuery;
		SearchService.ParseResult parseResult;
		try {

			jqlQuery = StaticParams.project + StaticParams.equalD
					+ proj.getKey() + StaticParams.andD
					+ StaticParams.issuetype + StaticParams.equalD
					+ itM.getIssueType(issueType).getName();
			final String logInfo = "com.winagile.demo.jira.reports Info: JQL : "
					+ jqlQuery;
			System.out.println(logInfo);
			log.error(logInfo);
			parseResult = ss.parseQuery(auser, jqlQuery);
		} catch (Exception e2) {
			e2.printStackTrace();
			log.error(StaticParams.getPrintStack(e2));
			throw new MyException("time", "demogadget.timefielderror");
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
				throw new MyException("time", "demogadget.timefielderror");
			} catch (Exception e2) {
				e2.printStackTrace();

				log.error(StaticParams.getPrintStack(e2));
				throw new MyException("time", "demogadget.timefielderror");
			}

			dataset.addValue(
					timeMap.get(StaticParams.totalTime)
							/ timeMap.get(StaticParams.totalNum), series,
					proj.getName());
		} else {
			System.out
					.println("com.winagile.demo.jira.reports ERROR: JQL is not valid");
		}
	}
}
