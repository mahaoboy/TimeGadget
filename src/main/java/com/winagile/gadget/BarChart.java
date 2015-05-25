package com.winagile.gadget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

/**
 * A simple demonstration application showing how to create a bar chart.
 * 
 */
public class BarChart {

	public String generateChart(int width, int height, Long Aid, Long Bid) {
		final CategoryDataset categoryDataset = createDataset(Aid, Bid);
		JFreeChart chart = createChart(categoryDataset);
		ChartHelper helper = new ChartHelper(chart);
		try {
			helper.generate(width, height);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return helper.getLocation();
	}

	private CategoryDataset createDataset(Long Aid, Long Bid) {

		// row keys...
		final String series1 = "First";
		User auser = ApplicationUsers.toDirectoryUser(ComponentAccessor
				.getJiraAuthenticationContext().getUser());
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		final CustomFieldManager cfm = ComponentAccessor
				.getCustomFieldManager();
		final CustomField Acf = cfm.getCustomFieldObject(Aid);
		final CustomField Bcf = cfm.getCustomFieldObject(Bid);

		final SearchService searchService = ComponentAccessor
				.getComponent(SearchService.class);

		Options options = ComponentAccessor.getOptionsManager().getOptions(
				Bcf.getConfigurationSchemes().listIterator().next()
						.getOneAndOnlyConfig());
		for (Option op : options.getRootOptions()) {
			double totalTime = 0;
			int totalNum = 0;

			String jqlQuery = "\"" + Bcf.getFieldName() + "\" = \""
					+ op.getValue() + "\"";
			System.out.println("com.winagile.demo.jira.reports Info: JQL : "
					+ jqlQuery);
			SearchService.ParseResult parseResult = searchService.parseQuery(
					auser, jqlQuery);

			if (parseResult.isValid()) {
				Query query = parseResult.getQuery();
				SearchResults results;
				try {
					results = searchService.search(auser, query,
							PagerFilter.getUnlimitedFilter());
					List<Issue> issues = results.getIssues();
					for (Issue issue : issues) {
						Object Avalue = Acf.getValue(issue);
						if (Avalue != null) {
							totalTime += (Double) Avalue;
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
				}

				dataset.addValue(totalTime / totalNum, series1, op.getValue());
			} else {
				System.out
						.println("com.winagile.demo.jira.reports ERROR: JQL is not valid");
			}
		}
		return dataset;

	}

	private JFreeChart createChart(final CategoryDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createBarChart("", // chart
																	// title
				"Category", // domain axis label
				"Average Time", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				false, // include legend
				false, // tooltips?
				false // URLs?
				);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		// set up gradient paints for series...
		final GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue,
				0.0f, 0.0f, Color.lightGray);
		// final GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green,
		// 0.0f, 0.0f, Color.lightGray);
		// final GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red,
		// 0.0f, 0.0f, Color.lightGray);
		renderer.setSeriesPaint(0, gp0);
		// renderer.setSeriesPaint(1, gp1);
		// renderer.setSeriesPaint(2, gp2);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;

	}

}
