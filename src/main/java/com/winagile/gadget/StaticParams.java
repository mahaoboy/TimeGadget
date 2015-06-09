package com.winagile.gadget;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.axis.encoding.Base64;
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

import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;

public class StaticParams {
	final public static int REPORT_IMAGE_WIDTH = 600;
	final public static int REPORT_IMAGE_HEIGHT = 600;

	final public static String selectKey = "com.atlassian.jira.plugin.system.customfieldtypes:select";
	final public static String priorityValue = "priority";
	final public static String project = "project";
	final public static String issuetype = "issuetype";
	final public static String issuestatus = "status";
	final public static String priorityName = "gadget.winagle.config.prio";
	final public static String delimeter = " -> ";
	final public static String delimeterV = "-";
	final public static String priorityitemHE = "Highest";
	final public static String priorityNameHE = "gadget.winagle.config.prioHE";
	final public static String priorityitemH = "High";
	final public static String priorityNameH = "gadget.winagle.config.prioH";
	final public static String priorityitemM = "Medium";
	final public static String priorityNameM = "gadget.winagle.config.prioM";
	final public static String priorityitemL = "Low";
	final public static String priorityNameL = "gadget.winagle.config.prioL";
	final public static String priorityitemLE = "Lowest";
	final public static String priorityNameLE = "gadget.winagle.config.prioLE";

	final public static String equalD = " = ";
	final public static String unequalD = " != ";
	final public static String andD = " and ";
	final public static String quoteD = "\"";

	final public static String critical = "Critical";
	final public static String normal = "Normal";

	public static String getPrintStack(Throwable e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

	public static ErrorCollection getErrorRep(MyException e, Logger log,
			String field) {
		e.printStackTrace();

		log.error(StaticParams.getPrintStack(e));
		SimpleErrorCollection sec = new SimpleErrorCollection();
		sec.addError("field", field);
		if (e != null && e.getMessage() != null && !e.getMessage().isEmpty()) {
			sec.addError("error", e.getMessage());
		} else {
			sec.addError("error", "demogadget.fielderror");
		}
		ErrorCollection ec = new ErrorCollection();
		ec.addErrorMessage(StaticParams.getPrintStack(e));
		ec.addErrorCollection(sec);

		return ec;
	}

	public static ErrorCollection getDoubleErrorRep(MyException e, Logger log,
			String field, String field2) {
		e.printStackTrace();

		log.error(StaticParams.getPrintStack(e));
		SimpleErrorCollection sec = new SimpleErrorCollection();
		sec.addError("field", field2);
		if (e != null && e.getMessage() != null && !e.getMessage().isEmpty()) {
			sec.addError("error", e.getMessage());
		} else {
			sec.addError("error", "demogadget.timefielderror");
		}
		sec.addError("field2", field);
		if (e != null && e.getMessage() != null && !e.getMessage().isEmpty()) {
			sec.addError("error2", e.getMessage());
		} else {
			sec.addError("error2", "demogadget.timefielderror");
		}
		ErrorCollection ec = new ErrorCollection();
		ec.addErrorMessage(StaticParams.getPrintStack(e));
		ec.addErrorCollection(sec);

		return ec;
	}

	public static void checkLicenseStatus(PluginLicenseManager licenseManager,
			I18nResolver i18n) throws MyException {
		if (licenseManager == null || i18n == null) {
			throw new MyException("License manager or i18n is null");
		}
		if (licenseManager.getLicense() == null
				|| !licenseManager.getLicense().iterator().hasNext()) {
			throw new MyException(
					i18n.getText("demogadget.exception.licenseerror"));
		}
		for (PluginLicense pluginLicense : licenseManager.getLicense()) {
			if (pluginLicense.getError().isDefined()) {
				throw new MyException(
						i18n.getText("demogadget.exception.licenseerror"));
			}
		}
	}

	public static PieChart createPieChart(int width, int height,
			JFreeChart jchart, CategoryDataset categoryDataset) {
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

		String displayName = "xx";

		PieChart pieChart = new PieChart(location, title, filterUrl,
				displayName, imageMap, imageMapName, chartWidth, chartHeight,
				chart.getBase64Image());
		return pieChart;
	}

	public static String imgToB64(BufferedImage paramBufferedImage) {
		// TODO Auto-generated method stub
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

	public static JFreeChart createChart(final CategoryDataset dataset) {
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
