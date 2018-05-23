/**
 * 
 */
package imago.plugin.image.analyze;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import imago.gui.ImagoChartFrame;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.color.RGB16Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.process.Histogram;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;
import net.sci.table.DataTable;

/**
 * @author David Legland
 *
 */
public class ImageHistogram implements Plugin
{
	public ImageHistogram()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// Check type is image frame
		if (!(frame instanceof ImagoDocViewer))
			return;
		ImagoDocViewer iframe = (ImagoDocViewer) frame;
		Image image = iframe.getDocument().getImage();

		DataTable histo = computeHistogram(image);
		
        int nChannels = histo.getColumnNumber();
        if (nChannels == 2)
        {
            showIntensityHistogram(frame, histo);
        } 
        else if (nChannels == 4)
        {
            showColorHistogram(frame, histo);
        }
	}

	private DataTable computeHistogram(Image image)
	{
		Array<?> array = image.getData();
		if (array instanceof RGB8Array)            
        {
            return histogram((RGB8Array) array);
        }
        else if (array instanceof ScalarArray)
		{
			double[] range = image.getDisplayRange();
			System.out.println(String.format("Display range for histogram: (%f ; %f)", range[0], range[1]));
			return histogram((ScalarArray<?>) array, range, 256);
		}
        else if (array instanceof RGB16Array)            
        {
            return histogramRGB16((RGB16Array) array);
        }
		else
		{
			throw new RuntimeException("Unable to compute histogram for array class: " + array.getClass());
		}
	}
	
	/**
	 * Computes histogram of a scalar array, and returns the result in a data
	 * table. The result is stored in a data table with two columns. The first
	 * column contains the position of the bin center. The second column
	 * contains the count of array element for the corresponding bin.
	 * 
	 * @param array
	 *            the input array
	 * @param range
	 *            the range of values for histogram computation
	 * @param nBins
	 *            the number of bins of the resulting histogram
	 * @return a new instance of DataTable containing the resulting histogram
	 */
	public static final DataTable histogram(ScalarArray<?> array, double[] range, int nBins)
	{
		// compute the sizeX of an individual bin
		double binWidth = (range[1] - range[0]) / (nBins - 1);
		
		// allocate memory for result
		int[] histo = Histogram.histogram(array, range, nBins);

		// format the result into data table
		DataTable table = new DataTable(nBins, 2);
		for (int i = 0; i < nBins; i++)
		{
			table.setValue(i, 0, range[0] + i * binWidth);
			table.setValue(i, 1, histo[i]);
		}
		
		table.setColumnNames(new String[]{"Intensity", "Count"});
		
		return table;
	}
	
    /**
     * Computes histogram of an array of RGB8 elements, and returns the result
     * in a data table.
     * 
     * The data table has four columns. The first column contains the bin center
     * (from 0 to 255). The three other columns contain the count of the
     * corresponding red, green and blue channels respectively.
     * 
     * @param array
     *            the input array of RGB8 elements
     * @return a new instance of DataTable containing the resulting histogram.
     */
    public static final DataTable histogram(RGB8Array array)
    {
        // allocate memory for result
        int[][] histo = Histogram.histogram(array);

        // format the result into data table
        DataTable table = new DataTable(256, 4);
        for (int i = 0; i < 256; i++)
        {
            table.setValue(i, 0, i);
            table.setValue(i, 1, histo[0][i]);
            table.setValue(i, 2, histo[1][i]);
            table.setValue(i, 3, histo[2][i]);
        }
        
        table.setColumnNames(new String[]{"Value", "Red", "Green", "Blue"});
        
        return table;
    }
    
    /**
     * Computes histogram of an array of RGB16 elements, and returns the result
     * in a data table.
     * 
     * The data table has four columns. The first column contains the bin
     * center. The three other columns contain the count of the corresponding
     * red, green and blue channels respectively.
     * 
     * @param array
     *            the input array of RGB16 elements
     * @return a new instance of DataTable containing the resulting histogram.
     */
    public static final DataTable histogramRGB16(RGB16Array array)
    {
        // allocate memory for result
        int[][] histo = Histogram.histogramRGB16(array);

        // format the result into data table
        DataTable table = new DataTable(256, 4);
        for (int i = 0; i < 256; i++)
        {
            table.setValue(i, 0, histo[0][i]);
            table.setValue(i, 1, histo[1][i]);
            table.setValue(i, 2, histo[2][i]);
            table.setValue(i, 3, histo[3][i]);
        }
        
        table.setColumnNames(new String[]{"Value", "Red", "Green", "Blue"});
        
        return table;
    }

	/**
	 * Display histogram of 256 gray scale images.
	 */
	private void showIntensityHistogram(ImagoFrame parentFrame, DataTable histo)
	{
		int nValues = histo.getRowNumber();

		// count element number
		int nElements = 0;
		for (int i = 0; i < nValues; i++)
		{
			nElements += histo.getValue(i, 1);
		}
		
		double binWidth = histo.getValue(1, 0) - histo.getValue(0, 0);
		double halfWidth = binWidth * .49;
		
		// determine if first bin (usually background) should be displayed
		// TODO: same for last bin ?
		boolean showFirstBin = histo.getValue(0, 1) < .1 * nElements;

		// Create resulting data set instance
		SimpleHistogramDataset dataset = new SimpleHistogramDataset("Image Values");

		// create a new histogram bin for each value
		int firstIndex = showFirstBin ? 0 : 1; 
		for (int i = firstIndex; i < nValues; i++)
		{
			double binStart = histo.getValue(i, 0) - halfWidth;
			double binEnd = histo.getValue(i, 0) + halfWidth;
			SimpleHistogramBin bin = new SimpleHistogramBin(binStart, binEnd, true, false);
			bin.setItemCount((int) histo.getValue(i, 1));
			dataset.addBin(bin);
		}

		// Title of the plot
		ImagoDocViewer iframe = (ImagoDocViewer) parentFrame;
		Image image = iframe.getDocument().getImage();
		String imageName = image.getName();
		String titleString;
		if (imageName == null)
			titleString = "Image Histogram";
		else
			titleString = "Histogram of " + imageName;

		// creates the chart
		JFreeChart chart = ChartFactory.createHistogram(titleString,
				"Pixel value", "Pixel Count", dataset, PlotOrientation.VERTICAL,
				true, true, true);
		chart.getPlot().setBackgroundPaint(Color.WHITE);

		XYBarRenderer renderer = (XYBarRenderer) chart.getXYPlot()
				.getRenderer();
		renderer.setBasePaint(Color.BLUE);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setShadowVisible(false);

		chart.fireChartChanged();

		// we put the chart into a panel
		ChartPanel chartPanel = new ChartPanel(chart, 512, 200, 512, 200, 512,
				512, false, false, true, true, true, true);

		// default size
		chartPanel.setPreferredSize(new java.awt.Dimension(512, 270));

		// creates a new frame to contains the chart panel
		ImagoChartFrame frame = new ImagoChartFrame(parentFrame, "Intensity Histogram");
		frame.getWidget().setContentPane(chartPanel);
		frame.getWidget().pack();
		frame.setVisible(true);
	}

	private void showColorHistogram(ImagoFrame parentFrame, DataTable histo)
	{
		int nChannels = histo.getColumnNumber() - 1;
		int nValues = histo.getRowNumber();

		XYSeriesCollection dataset = new XYSeriesCollection();
		String[] colNames = histo.getColumnNames();

		for (int c = 0; c < nChannels; c++)
		{
			// create a new series for each channel
			XYSeries series = new XYSeries(colNames[c + 1]);
			for (int i = 0; i < nValues; i++)
			{
				series.add(histo.getValue(i, 0), histo.getValue(i, c + 1));
			}

			// add the series to the data set
			dataset.addSeries(series);
		}

		// Title of the plot
		ImagoDocViewer iframe = (ImagoDocViewer) parentFrame;
		Image image = iframe.getDocument().getImage();
		String imageName = image.getName();
		String titleString;
		if (imageName == null)
			titleString = "Image Histogram";
		else
			titleString = "Histogram of " + imageName;

		// creates the chart
		JFreeChart chart = ChartFactory.createXYStepChart(titleString,
				"Pixel value", // domain axis label
				"Pixel Count", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, true, true);

		// get a reference to the plot for further customisation
		XYPlot plot = chart.getXYPlot();
		plot.setDomainAxis(new NumberAxis());

		// change the auto tick unit selection to integer units only.
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// set the color for each series
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesPaint(1, Color.GREEN);
		renderer.setSeriesPaint(2, Color.BLUE);

		
		chart.fireChartChanged();

		// put the chart into a panel
		ChartPanel chartPanel = new ChartPanel(chart, 512, 200, 512, 200, 512,
				512, false, false, true, true, true, true);

		// default size
		chartPanel.setPreferredSize(new java.awt.Dimension(512, 270));

		// creates a new frame to contains the chart panel
		ImagoChartFrame frame = new ImagoChartFrame(parentFrame, "Color Histogram");
		frame.getWidget().setContentPane(chartPanel);
		frame.getWidget().pack();
		frame.setVisible(true);
	}
}
