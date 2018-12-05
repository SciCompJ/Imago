/**
 * 
 */
package imago.plugin.image.analyze;

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

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.markers.SeriesMarkers;

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
			double[] range = image.getDisplaySettings().getDisplayRange();
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
	private void showIntensityHistogram(ImagoFrame parentFrame, DataTable table)
	{
        // Title of the plot
        ImagoDocViewer iframe = (ImagoDocViewer) parentFrame;
        Image image = iframe.getDocument().getImage();
		String titleString = createTitleString("Histogram", image.getName());

		// Create Chart
	    XYChart chart = new XYChartBuilder()
	            .width(800)
	            .height(600)
	            .title(titleString)
	            .xAxisTitle("Intensity Level")
	            .yAxisTitle("Frequency")
	            .build();
	 
	    // Customize Chart
	    chart.getStyler().setLegendVisible(false);
	    chart.getStyler().setPlotGridLinesVisible(false);
	    chart.getStyler().setPlotGridVerticalLinesVisible(false);

	    // Series
        double[] xdata = table.getColumnValues(0);
        double[] ydata = table.getColumnValues(1);
        XYSeries series = chart.addSeries("Histogram", xdata, ydata);
        series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
        series.setMarker(SeriesMarkers.NONE);
     
        ImagoChartFrame.displayChart(parentFrame, "Histogram", chart);
	}

	private void showColorHistogram(ImagoFrame parentFrame, DataTable table)
	{
		int nChannels = table.getColumnNumber() - 1;
		int nValues = table.getRowNumber();
        String[] colNames = table.getColumnNames();
        
        // Default name for table
        String tableName = table.getName();
        if (tableName == null || tableName.length() == 0)
        {
            tableName = "data";
        }

        // Title of the plot
        ImagoDocViewer iframe = (ImagoDocViewer) parentFrame;
        Image image = iframe.getDocument().getImage();
        String titleString = createTitleString("Histogram", image.getName());
        
        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(titleString)
                .xAxisTitle("Channel value")
                .yAxisTitle("Frequency")
                .build();
        
        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        
        // create a new series for each channel
        double[] xData = generateLinearVector(nValues, 0);
        XYSeries[] series  = new XYSeries[nChannels];
        for (int c = 0; c < nChannels; c++)
        {
            series[c] = chart.addSeries(colNames[c], xData, table.getColumnValues(c+1));
            series[c].setMarker(SeriesMarkers.NONE);
            
        }
        
        // changes default colors
        series[0].setLineColor(XChartSeriesColors.RED);
        series[1].setLineColor(XChartSeriesColors.GREEN);
        series[2].setLineColor(XChartSeriesColors.BLUE);
        
        // Show it
        ImagoChartFrame.displayChart(parentFrame, "Color Histogram", chart);
	}

//    private double[] convertXData(double[] input)
//    {
//        int n = input.length;
//        if (n < 2)
//        {
//            throw new IllegalArgumentException("Requires at least two inputs");
//        }
//        
//        double[] output = new double[n*2];
//        
//        output[0] = input[0] - (input[1] - input[0]) / 2;
//        for (int i = 1; i < n-1; i++)
//        {
//            output[2*i - 1] = input[i] - 1e-8;
//            output[2*i] = input[i] + 1e-8;
//        }
//        output[2*n-1] = input[n-1] + (input[n-1] - input[n-2]) / 2;
//
//        return output;
//    }
//    
//    private double[] convertYData(double[] input)
//    {
//        int n = input.length;
//        if (n < 2)
//        {
//            throw new IllegalArgumentException("Requires at least two inputs");
//        }
//        
//        double[] output = new double[n*2];
//        
//        for (int i = 0; i < n; i++)
//        {
//            output[2 * i] = input[i];
//            output[2 * i + 1] = input[i];
//        }
//
//        return output;
//    }
    
	private String createTitleString(String baseTitle, String imageName)
    {
        if (imageName != null)
        {
            baseTitle += " of " + imageName;
        }
        return baseTitle;
    }
    
    /**
     * Generate a linear vectors containing values starting from 1, 2... to
     * nRows.
     * 
     * @param nRows
     *            the number of values
     * @return a linear vector of nRows values
     */
    private double[] generateLinearVector(int nRows, double startValue)
    {
        double[] values = new double[nRows];
        for (int i = 0; i < nRows; i++)
        {
            values[i] = startValue + i;
        }
        return values;
    }
   
}
