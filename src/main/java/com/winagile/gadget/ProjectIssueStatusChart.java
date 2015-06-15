package com.winagile.gadget;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.StatusManager;
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

public class ProjectIssueStatusChart {
	final private IssueTypeManager itM;
	final private SearchService ss;
	final private ProjectManager pM;
	final private StatusManager sM;
	private String issueType;
	private String solvestatus;
	private User auser;

	final private PluginLicenseManager licenseManager;
	final private I18nResolver i18n;

	private static final Logger log = LogManager
			.getLogger(ProjectIssueStatusChart.class);

	ProjectIssueStatusChart(IssueTypeManager itM, SearchService ss,
			ProjectManager pM, StatusManager sM,
			PluginLicenseManager licenseManager, I18nResolver i18n) {
		this.itM = itM;
		this.ss = ss;
		this.pM = pM;
		this.sM = sM;
		this.licenseManager = licenseManager;
		this.i18n = i18n;
	}

	public StaticParams.PieChart generateChart(int width, int height,
			String solvestatus, String issueType) throws MyException {

		StaticParams.checkLicenseStatus(licenseManager, i18n);
		this.issueType = issueType;
		this.solvestatus = solvestatus;
		final CategoryDataset categoryDataset = createDataset();
		JFreeChart jchart = StaticParams.createChart(categoryDataset);

		return StaticParams.createPieChart(width, height, jchart,
				categoryDataset, "");
	}

	private CategoryDataset createDataset() throws MyException {
		final String series1 = "First";
		final String series2 = "Second";
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (Project proj : pM.getProjectObjects()) {
			getDataSet(dataset, proj, series1,
					getPrioString(solvestatus, StaticParams.equalD));
			getDataSet(dataset, proj, series2,
					getPrioString(solvestatus, StaticParams.unequalD));
		}
		return dataset;

	}

	private String getPrioString(String statusid, String de) {
		String prioField;
		prioField = StaticParams.issuestatus + de + StaticParams.quoteD
				+ sM.getStatus(solvestatus).getName() + StaticParams.quoteD;
		return prioField;
	}

	private void getDataSet(DefaultCategoryDataset dataset, Project proj,
			String series, String extraJQL) throws MyException {
		auser = ApplicationUsers.toDirectoryUser(ComponentAccessor
				.getJiraAuthenticationContext().getUser());

		double totalIssue = 0;
		String jqlQuery;
		SearchService.ParseResult parseResult;
		try {

			jqlQuery = StaticParams.project + StaticParams.equalD
					+ proj.getKey() + StaticParams.andD
					+ StaticParams.issuetype + StaticParams.equalD
					+ itM.getIssueType(issueType).getName() + StaticParams.andD
					+ extraJQL;
			final String logInfo = "com.winagile.demo.jira.reports Info: JQL : "
					+ jqlQuery;
			System.out.println(logInfo);
			log.error(logInfo);
			parseResult = ss.parseQuery(auser, jqlQuery);
		} catch (Exception e2) {
			e2.printStackTrace();
			log.error(StaticParams.getPrintStack(e2));
			throw new MyException("statusId", "demogadget.fielderror");
		}

		if (parseResult.isValid()) {
			Query query = parseResult.getQuery();
			SearchResults results;
			try {
				results = ss.search(auser, query,
						PagerFilter.getUnlimitedFilter());
				List<Issue> issues = results.getIssues();
				totalIssue += issues.size();
			} catch (SearchException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();

				log.error(StaticParams.getPrintStack(e1));
				throw new MyException("statusId", "demogadget.fielderror");
			} catch (Exception e2) {
				e2.printStackTrace();

				log.error(StaticParams.getPrintStack(e2));
				throw new MyException("statusId", "demogadget.fielderror");
			}

			dataset.addValue(totalIssue, series, proj.getName());
		} else {
			System.out
					.println("com.winagile.demo.jira.reports ERROR: JQL is not valid");
		}
	}

}
