package com.winagile.gadget;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
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

public class ProjectIssueStatusChart {
	final private IssueTypeManager itM;
	final private SearchService ss;
	final private ProjectManager pM;
	final private StatusManager sM;
	private String issueType;
	private String solvestatus;
	private User auser;

	private static final Logger log = LogManager
			.getLogger(ProjectIssueStatusChart.class);

	ProjectIssueStatusChart(IssueTypeManager itM, SearchService ss,
			ProjectManager pM, StatusManager sM) {
		this.itM = itM;
		this.ss = ss;
		this.pM = pM;
		this.sM = sM;
	}

	public PieChart generateChart(int width, int height, String solvestatus,
			String issueType) throws MyException {
		this.issueType = issueType;
		this.solvestatus = solvestatus;
		final CategoryDataset categoryDataset = createDataset();
		JFreeChart jchart = createChart(categoryDataset);
		ChartHelper helper = new ChartHelper(jchart);
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			helper.generateInline(width, height);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
		}
		params.put("chart", helper.getLocation());
		params.put("chartDataset", categoryDataset);
		params.put("imagemap", helper.getImageMapHtml());
		params.put("imagemapName", helper.getImageMapName());
		params.put("width", width);
		params.put("height", height);
		params.put("base64Image", imgToB64(helper.getImage()));

		Chart chart = new Chart(helper.getLocation(), helper.getImageMapHtml(),
				helper.getImageMapName(), params);

		Map<String, Object> chartParams = chart.getParameters();
		String location = helper.getLocation();
		String title = "Title";
		String filterUrl = "url";
		int chartWidth = ((Integer) chartParams.get("width")).intValue();
		int chartHeight = ((Integer) chartParams.get("height")).intValue();
		String imageMap = chart.getImageMap();
		String imageMapName = chart.getImageMapName();

		String displayName = "xx";

		PieChart pieChart = new PieChart(location, title, filterUrl,
				displayName, imageMap, imageMapName, chartWidth, chartHeight,
				chart.getBase64Image());

		return pieChart;
	}

	private String imgToB64(BufferedImage paramBufferedImage) {
		// TODO Auto-generated method stub
		BufferedImage bi = paramBufferedImage;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "PNG", out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(StaticParams.getPrintStack(e));
		}
		byte[] bytes = out.toByteArray();
		String src = "data:image/png;base64," + Base64.encode(bytes);
		return src;
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
			throw new MyException("statusId", "unit field error");
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
				throw new MyException("statusId", "Search Error");
			} catch (Exception e2) {
				e2.printStackTrace();

				log.error(StaticParams.getPrintStack(e2));
				throw new MyException("statusId", "time field error");
			}

			dataset.addValue(totalIssue, series, proj.getName());
		} else {
			System.out
					.println("com.winagile.demo.jira.reports ERROR: JQL is not valid");
		}
	}

	private JFreeChart createChart(final CategoryDataset dataset) {
		// 创建主题样式
		StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
		// 设置标题字体
		standardChartTheme.setExtraLargeFont(new Font("隶书", Font.BOLD, 20));
		// 设置图例的字体
		standardChartTheme.setRegularFont(new Font("宋书", Font.PLAIN, 15));
		// 设置轴向的字体
		standardChartTheme.setLargeFont(new Font("宋书", Font.PLAIN, 15));
		// 应用主题样式
		ChartFactory.setChartTheme(standardChartTheme);
		// create the chart...
		final JFreeChart chart = ChartFactory.createBarChart("", // chart
																	// title
				"", // domain axis label
				"", // range axis label
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
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// rangeAxis.setTickLabelFont(new Font("Times New Roman", Font.PLAIN,
		// 12));
		// rangeAxis.setLabelFont(new Font("Times New Roman", Font.PLAIN, 12));
		// disable bar outlines...
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		// set up gradient paints for series...
		final GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, new Color(79,
				129, 189), 0.0f, 0.0f, new Color(79, 129, 189));
		final GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, new Color(192,
				80, 77), 0.0f, 0.0f, new Color(192, 80, 77));
		// final GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red,
		// 0.0f, 0.0f, Color.lightGray);
		renderer.setSeriesPaint(0, gp0);
		renderer.setSeriesPaint(1, gp1);
		// renderer.setSeriesPaint(2, gp2);
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
		final CategoryAxis domainAxis = plot.getDomainAxis();
		renderer.setItemMargin(0.0);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));

		// domainAxis.setTickLabelFont(new Font("Times New Roman", Font.PLAIN,
		// 12));
		// domainAxis.setLabelFont(new Font("Times New Roman", Font.PLAIN, 12));
		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;

	}

	@XmlRootElement
	public static class PieChart {

		@XmlElement
		private String location;

		@XmlElement
		private String filterTitle;

		@XmlElement
		private String filterUrl;

		@XmlElement
		private long issueCount;

		@XmlElement
		private String statType;

		@XmlElement
		private String imageMap;

		@XmlElement
		private String imageMapName;

		@XmlElement
		private int height;

		@XmlElement
		private int width;

		@XmlElement
		private String base64Image;

		private PieChart() {
		}

		PieChart(String location, String filterTitle, String filterUrl,
				String statType, String imageMap, String imageMapName,
				int width, int height, String base64Image) {
			this.location = location;
			this.filterTitle = filterTitle;
			this.filterUrl = filterUrl;
			this.statType = statType;
			this.imageMap = imageMap;
			this.imageMapName = imageMapName;
			this.width = width;
			this.height = height;
			this.base64Image = base64Image;
		}
	}

}
