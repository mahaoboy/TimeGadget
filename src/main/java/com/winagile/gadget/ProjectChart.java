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
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;

public class ProjectChart {
	final private IssueTypeManager itM;
	final private CustomFieldManager customFM;
	final private ProjectManager pm;
	final private SearchService ss;
	final private OptionsManager opM;
	private Long Aid;
	private Long Cid;
	private String issueType;
	private User auser;
	private String timeunitId;
	final private PluginLicenseManager licenseManager;
	final private I18nResolver i18n;

	private static final Logger log = LogManager.getLogger(ProjectChart.class);

	ProjectChart(CustomFieldManager customFM, IssueTypeManager itM,
			SearchService ss, OptionsManager opM, ProjectManager pm,
			PluginLicenseManager licenseManager, I18nResolver i18n) {
		this.customFM = customFM;
		this.itM = itM;
		this.ss = ss;
		this.opM = opM;
		this.pm = pm;
		this.licenseManager = licenseManager;
		this.i18n = i18n;
	}

	public StaticParams.PieChart generateChart(int width, int height, Long Aid,
			Long Cid, String issueType, String prio, String timeunitId)
			throws MyException {

		StaticParams.checkLicenseStatus(licenseManager, i18n);
		this.issueType = issueType;
		this.Aid = Aid;
		this.Cid = Cid;
		this.timeunitId = timeunitId;
		final CategoryDataset categoryDataset = createDataset(prio);
		JFreeChart jchart = StaticParams.createChart(categoryDataset);
		return StaticParams.createPieChart(width, height, jchart,
				categoryDataset, "");
	}

	private CategoryDataset createDataset(String prio) throws MyException {
		final String series1 = StaticParams.critical;
		final String series2 = StaticParams.normal;
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		List<Project> pl = pm.getProjectObjects();
		final String name = prio.split(StaticParams.delimeterV)[0];
		final String value = prio.split(StaticParams.delimeterV)[1];

		for (Project proj : pl) {
			getDataSet(dataset, proj, series1,
					getPrioString(name, value, StaticParams.equalD));
			getDataSet(dataset, proj, series2,
					getPrioString(name, value, StaticParams.unequalD));
		}
		return dataset;

	}

	private String getPrioString(String name, String value, String de) {
		String prioField;
		if (name.equals(StaticParams.priorityValue)) {
			prioField = name + de + value;
		} else {
			prioField = StaticParams.quoteD
					+ customFM.getCustomFieldObject(name).getFieldName()
					+ StaticParams.quoteD + de + StaticParams.quoteD
					+ opM.findByOptionId(Long.parseLong(value)).getValue()
					+ StaticParams.quoteD;
		}
		return prioField;
	}

	private void getDataSet(DefaultCategoryDataset dataset, Project proj,
			String series, String prioField) throws MyException {
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
					+ itM.getIssueType(issueType).getName() + StaticParams.andD
					+ prioField;
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
