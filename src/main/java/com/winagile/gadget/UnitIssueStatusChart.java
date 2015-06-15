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

public class UnitIssueStatusChart {
	final private IssueTypeManager itM;
	final private CustomFieldManager customFM;
	final private SearchService ss;
	final private OptionsManager opM;
	final private StatusManager sM;
	private CustomField Bcf;
	private String issueType;
	private String solvestatus;
	private User auser;
	private String projectId;
	final private ProjectManager pm;
	final private PluginLicenseManager licenseManager;
	final private I18nResolver i18n;

	private static final Logger log = LogManager
			.getLogger(UnitIssueStatusChart.class);

	UnitIssueStatusChart(CustomFieldManager customFM, IssueTypeManager itM,
			SearchService ss, OptionsManager opM, StatusManager sM,
			PluginLicenseManager licenseManager, I18nResolver i18n,
			ProjectManager pm) {
		this.customFM = customFM;
		this.itM = itM;
		this.ss = ss;
		this.opM = opM;
		this.sM = sM;
		this.licenseManager = licenseManager;
		this.i18n = i18n;
		this.pm = pm;
	}

	public StaticParams.PieChart generateChart(int width, int height,
			String solvestatus, Long Bid, String issueType, String projectId)
			throws MyException {

		StaticParams.checkLicenseStatus(licenseManager, i18n);
		this.issueType = issueType;
		this.solvestatus = solvestatus;
		this.projectId = projectId;
		final CategoryDataset categoryDataset = createDataset(Bid);
		JFreeChart jchart = StaticParams.createChart(categoryDataset);
		return StaticParams
				.createPieChart(width, height, jchart, categoryDataset,
						projectId.equals(StaticParams.allProject) ? "" : pm
								.getProjectObj(Long.parseLong(projectId))
								.getName());
	}

	private CategoryDataset createDataset(Long Bid) throws MyException {
		final String series1 = "First";
		final String series2 = "Second";
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Bcf = customFM.getCustomFieldObject(Bid);

		Options options;
		try {
			options = opM.getOptions(Bcf.getConfigurationSchemes()
					.listIterator().next().getOneAndOnlyConfig());
			if (options.isEmpty()) {
				throw new Exception("No option found");
			}
		} catch (Exception e3) {
			e3.printStackTrace();
			log.error(StaticParams.getPrintStack(e3));
			throw new MyException("unit", "demogadget.fielderror");
		}

		for (Option op : options.getRootOptions()) {
			getDataSet(dataset, op, series1,
					getPrioString(solvestatus, StaticParams.equalD));
			getDataSet(dataset, op, series2,
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

	private void getDataSet(DefaultCategoryDataset dataset, Option op,
			String series, String extraJQL) throws MyException {
		auser = ApplicationUsers.toDirectoryUser(ComponentAccessor
				.getJiraAuthenticationContext().getUser());

		double totalIssue = 0;
		String jqlQuery;
		SearchService.ParseResult parseResult;
		try {

			jqlQuery = StaticParams.quoteD + Bcf.getFieldName()
					+ StaticParams.quoteD + StaticParams.equalD
					+ StaticParams.quoteD + op.getValue() + StaticParams.quoteD
					+ StaticParams.andD + StaticParams.issuetype
					+ StaticParams.equalD
					+ itM.getIssueType(issueType).getName() + StaticParams.andD
					+ extraJQL;
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
				totalIssue += issues.size();
			} catch (SearchException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();

				log.error(StaticParams.getPrintStack(e1));
				throw new MyException("unit", "demogadget.fielderror");
			} catch (Exception e2) {
				e2.printStackTrace();

				log.error(StaticParams.getPrintStack(e2));
				throw new MyException("time", "demogadget.fielderror");
			}

			dataset.addValue(totalIssue, series, op.getValue());
		} else {
			System.out
					.println("com.winagile.demo.jira.reports ERROR: JQL is not valid");
		}
	}

}