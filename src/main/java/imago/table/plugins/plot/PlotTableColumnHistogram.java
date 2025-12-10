/**
 * 
 */
package imago.table.plugins.plot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchart.Histogram;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.chart.ChartFrame;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.plugins.TableFramePlugin;
import net.sci.table.NumericColumn;
import net.sci.table.Table;


/**
 * Computes and display histogram of values within a column of the table within
 * the frame.
 * 
 * @author David Legland
 *
 */
public class PlotTableColumnHistogram implements TableFramePlugin
{
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Get the data table
        if (!(frame instanceof TableFrame))
        {
            return;
        }
        Table table = ((TableFrame) frame).getTable();
        // create a list of names corresponding to numeric columns
        String[] colNames = table.columns().stream()
                .filter(c -> c instanceof NumericColumn)
                .map(c -> c.getName())
                .toArray(String[]::new);
        if (colNames.length == 0)
        {
            throw new RuntimeException("Input table does not contain any numeric column");
        }
        
        // compute default values
        int indCol = 0;
        
        GenericDialog dlg = new GenericDialog(frame, "Histogram");
        dlg.addChoice("Column", colNames, colNames[0]);
        dlg.addNumericField("Bin Number", 20, 0);
        
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // retrieve user inputs
        indCol = dlg.getNextChoiceIndex();
        int binCount = (int) dlg.getNextNumber();
        NumericColumn col = (NumericColumn) table.column(table.findColumnIndex(colNames[indCol]));
               
        double[] data = col.getValues();
        double[] range = valueRange(data);
        List<Double> valueList = Arrays.stream(data).boxed().collect(Collectors.toList());
        Histogram histogram = new Histogram(valueList, binCount, range[0], range[1]);
        
        String chartTitle = "Histogram";
        if (col.getName() != null && col.getName().length() > 0)
        {
            chartTitle += " of " + col.getName();
        }
        
        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(chartTitle)
                .xAxisTitle(col.getName())
                .yAxisTitle("Count")
                .build();
        
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Area);
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisDecimalPattern("#0.00");
        
        XYSeries series = addHistogramSeries(chart, col.getName(), histogram);
        series.setMarker(SeriesMarkers.NONE);
        
        // Show it
        ChartFrame.create(chart, "Histogram", frame);
	}
    
    /**
     * Converts the histogram into a XYSeries suitable for display.
     * 
     * @param chart
     *            the chart to add the series to
     * @param name
     *            the name of the series, used for legend
     * @param histogram
     *            the histogram to convert
     * @return a XYSeries mimicking the histogram
     */
    private XYSeries addHistogramSeries(XYChart chart, String name, Histogram histogram)
    {
        double xmin = histogram.getMin();
        double xmax = histogram.getMax();
        int nBins = histogram.getNumBins();
        double binSize = (xmax - xmin) / nBins;
        
        List<Double> histoData = histogram.getyAxisData();
        
        // allocate data for new series
        double[] xData = new double[histoData.size() * 4];
        double[] yData = new double[histoData.size() * 4];
        for(int i = 0; i < histoData.size(); i++)
        {
            xData[4 * i]     = xmin + i * binSize;
            xData[4 * i + 1] = xmin + i * binSize;
            xData[4 * i + 2] = xmin + (i + 1) * binSize;
            xData[4 * i + 3] = xmin + (i + 1) * binSize;
            yData[4 * i] = 0;
            yData[4 * i + 1] = histoData.get(i);
            yData[4 * i + 2] = histoData.get(i);
            yData[4 * i + 3] = 0;
        }
        
        return chart.addSeries(name, xData, yData);
    }
    
    private static final double[] valueRange(double[] values)
    {
        double vmin = Double.POSITIVE_INFINITY;
        double vmax = Double.NEGATIVE_INFINITY;
        for (double v : values)
        {
            vmin = Math.min(v, vmin);
            vmax = Math.max(v, vmax);
        }
        return new double[] {vmin, vmax};
    }
}
