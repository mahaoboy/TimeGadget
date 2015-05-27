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
import javax.xml.bind.annotation.XmlType;

import org.apache.axis.encoding.Base64;
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

	public PieChart generateChart(int width, int height, Long Aid, Long Bid)
			throws MyException {
		final CategoryDataset categoryDataset = createDataset(Aid, Bid);
		JFreeChart jchart = createChart(categoryDataset);
		ChartHelper helper = new ChartHelper(jchart);
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			helper.generateInline(width, height);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		DataRow[] data = null;
		data = generateDataSet(categoryDataset);

		String displayName = "xx";

		PieChart pieChart = new PieChart(location, title, filterUrl,
				displayName, imageMap, imageMapName, data, chartWidth,
				chartHeight, chart.getBase64Image());

		return pieChart;
	}

	private String imgToB64(BufferedImage paramBufferedImage) {
		// TODO Auto-generated method stub
		System.out.println("param passed : " + paramBufferedImage);
		BufferedImage bi = paramBufferedImage;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "PNG", out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] bytes = out.toByteArray();
		String src = "data:image/png;base64," + Base64.encode(bytes);
		return src;
	}

	private DataRow[] generateDataSet(CategoryDataset dataset) {
		DataRow[] data = new DataRow[dataset.getColumnCount()];

		for (int col = 0; col < dataset.getColumnCount(); ++col) {
			Comparable key = dataset.getColumnKey(col);
			int val = dataset.getValue(0, col).intValue();
			int percentage = dataset.getValue(1, col).intValue();
			data[col] = new DataRow(key, val, percentage);
		}

		return data;
	}

	private CategoryDataset createDataset(Long Aid, Long Bid)
			throws MyException {

		// row keys...
		final String series1 = "First";
		final String series2 = "S";
		User auser = ApplicationUsers.toDirectoryUser(ComponentAccessor
				.getJiraAuthenticationContext().getUser());
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		final CustomFieldManager cfm = ComponentAccessor
				.getCustomFieldManager();
		final CustomField Acf = cfm.getCustomFieldObject(Aid);
		final CustomField Bcf = cfm.getCustomFieldObject(Bid);

		final SearchService searchService = ComponentAccessor
				.getComponent(SearchService.class);
		Options options;
		try {
			options = ComponentAccessor.getOptionsManager().getOptions(
					Bcf.getConfigurationSchemes().listIterator().next()
							.getOneAndOnlyConfig());
			if (options.isEmpty()) {
				throw new Exception();
			}
		} catch (Exception e3) {
			e3.printStackTrace();
			throw new MyException("unit", "unit field error");
		}

		for (Option op : options.getRootOptions()) {
			double totalTime = 0;
			int totalNum = 0;
			String jqlQuery;
			SearchService.ParseResult parseResult;
			try {
				jqlQuery = "\"" + Bcf.getFieldName() + "\" = \""
						+ op.getValue() + "\"";
				System.out
						.println("com.winagile.demo.jira.reports Info: JQL : "
								+ jqlQuery);
				parseResult = searchService.parseQuery(auser, jqlQuery);
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new MyException("unit", "unit field error");
			}

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
					throw new MyException("unit", "Search Error");
				} catch (Exception e2) {
					e2.printStackTrace();
					throw new MyException("time", "time field error");
				}

				dataset.addValue(totalTime / totalNum, series1, op.getValue());
				dataset.addValue(totalTime / totalNum, series2, op.getValue());
			} else {
				System.out
						.println("com.winagile.demo.jira.reports ERROR: JQL is not valid");
			}
		}
		return dataset;

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
		//rangeAxis.setTickLabelFont(new Font("Times New Roman", Font.PLAIN, 12));
		//rangeAxis.setLabelFont(new Font("Times New Roman", Font.PLAIN, 12));
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

		//domainAxis.setTickLabelFont(new Font("Times New Roman", Font.PLAIN, 12));
		//domainAxis.setLabelFont(new Font("Times New Roman", Font.PLAIN, 12));
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
		private BarChart.DataRow[] data;

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
				BarChart.DataRow[] data, int width, int height,
				String base64Image) {
			this.location = location;
			this.filterTitle = filterTitle;
			this.filterUrl = filterUrl;
			this.statType = statType;
			this.imageMap = imageMap;
			this.imageMapName = imageMapName;
			this.data = data;
			this.width = width;
			this.height = height;
			this.base64Image = base64Image;
		}
	}

	@XmlRootElement
	@XmlType(namespace = "com.winagile.gadget.BarChart")
	public static class DataRow {
		private Comparable key;

		@XmlElement
		private int value;

		@XmlElement
		private int pecentage;

		@XmlElement(name = "key")
		private String keyString;

		public DataRow() {
		}

		DataRow(Comparable key, int value, int pecentage) {
			this.key = key;
			this.value = value;
			this.pecentage = pecentage;
			this.keyString = key.toString();
		}

		public Comparable getRawKey() {
			return this.key;
		}

		public int hashCode() {
			int result = (this.key != null) ? this.key.hashCode() : 0;
			result = 31 * result + this.value;
			result = 31 * result + this.pecentage;
			return result;
		}
	}

}
