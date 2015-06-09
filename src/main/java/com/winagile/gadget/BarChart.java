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
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;

/**
 * A simple demonstration application showing how to create a bar chart.
 * 
 */
public class BarChart {

	final private IssueTypeManager itM;
	final private CustomFieldManager customFM;
	final private SearchService ss;
	final private OptionsManager opM;
	private CustomField Acf;
	private CustomField Ccf;
	private CustomField Bcf;
	private String issueType;
	private User auser;
	final private PluginLicenseManager licenseManager;
	final private I18nResolver i18n;

	private static final Logger log = LogManager.getLogger(BarChart.class);

	BarChart(CustomFieldManager customFM, IssueTypeManager itM,
			SearchService ss, OptionsManager opM,
			PluginLicenseManager licenseManager, I18nResolver i18n) {
		this.customFM = customFM;
		this.itM = itM;
		this.ss = ss;
		this.opM = opM;
		this.licenseManager = licenseManager;
		this.i18n = i18n;
	}

	public StaticParams.PieChart generateChart(int width, int height, Long Aid,
			Long Cid, Long Bid, String issueType, String prio)
			throws MyException {

		StaticParams.checkLicenseStatus(licenseManager, i18n);

		this.issueType = issueType;

		final CategoryDataset categoryDataset = createDataset(Aid, Cid, Bid,
				prio);
		JFreeChart jchart = StaticParams.createChart(categoryDataset);
		return StaticParams.createPieChart(width, height, jchart,
				categoryDataset);
	}

	private CategoryDataset createDataset(Long Aid, Long Cid, Long Bid,
			String prio) throws MyException {
		final String series1 = "First";
		final String series2 = "Second";
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Acf = customFM.getCustomFieldObject(Aid);
		Ccf = customFM.getCustomFieldObject(Cid);
		Bcf = customFM.getCustomFieldObject(Bid);
		final String name = prio.split(StaticParams.delimeterV)[0];
		final String value = prio.split(StaticParams.delimeterV)[1];

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
			getDataSet(dataset, op, series1,
					getPrioString(name, value, StaticParams.equalD));
			getDataSet(dataset, op, series2,
					getPrioString(name, value, StaticParams.unequalD));
		}
		return dataset;

	}

	private String getPrioString(String name, String value, String de) {
		String prioField;
		if (name.equals(StaticParams.priorityValue)) {
			prioField = name + de + value;
		} else {
			prioField = "\""
					+ customFM.getCustomFieldObject(name).getFieldName() + "\""
					+ de + "\""
					+ opM.findByOptionId(Long.parseLong(value)).getValue()
					+ "\"";
		}
		return prioField;
	}

	private void getDataSet(DefaultCategoryDataset dataset, Option op,
			String series, String prioField) throws MyException {
		auser = ApplicationUsers.toDirectoryUser(ComponentAccessor
				.getJiraAuthenticationContext().getUser());

		double totalTime = 0;
		int totalNum = 0;
		String jqlQuery;
		SearchService.ParseResult parseResult;
		try {

			jqlQuery = "\"" + Bcf.getFieldName() + "\" = \"" + op.getValue()
					+ "\" and issuetype = "
					+ itM.getIssueType(issueType).getName() + " and "
					+ prioField;
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
					Object Avalue = Acf.getValue(issue);
					Object Cvalue = Ccf.getValue(issue);
					log.error("com.winagile.demo.jira.reports ERROR: "
							+ Acf.getName() + " : " + Avalue + ","
							+ Ccf.getName() + " : " + Cvalue);
					if (Avalue != null && Cvalue != null) {

						totalTime += (Double) Cvalue - (Double) Avalue;
						totalNum++;
					} else {
						System.out
								.println("com.winagile.demo.jira.reports ERROR: "
										+ Acf.getName() + " : " + Avalue);
					}

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

			dataset.addValue(totalTime / totalNum, series, op.getValue());
		} else {
			System.out
					.println("com.winagile.demo.jira.reports ERROR: JQL is not valid");
		}
	}

}
