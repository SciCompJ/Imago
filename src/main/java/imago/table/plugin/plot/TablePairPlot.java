/**
 * 
 */
package imago.table.plugin.plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchart.Histogram;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoChartFrame;
import imago.table.TableFrame;
import imago.table.plugin.TableFramePlugin;
import net.sci.table.NumericColumn;
import net.sci.table.NumericTable;
import net.sci.table.Table;

/**
 * 
 */
public class TablePairPlot implements TableFramePlugin
{
    
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Get the data table
        if (!(frame instanceof TableFrame))
        {
            return;
        }
        Table table = ((TableFrame) frame).getTable();
        
        table = NumericTable.keepNumericColumns(table);
        
        int nCols = table.columnCount();
        if (nCols < 2)
        {
            throw new RuntimeException("Requires a table with at least two numeric columns");
        }
        
        
        List<XYChart> charts = new ArrayList<XYChart>();
        for (int y = 0; y < nCols; y++)
        {
            for (int x = 0; x < nCols; x++)
            {
                XYChartBuilder builder = new XYChartBuilder()
                        .width(100)
                        .height(80)
                        .theme(ChartTheme.Matlab);
                if (x == 0)
                {
                    builder.yAxisTitle(table.getColumnName(y));
                }
                if (y == nCols-1)
                {
                    builder.xAxisTitle(table.getColumnName(x));
                }
                XYChart chart = builder.build();
                
                if (x == y)
                {
                    // Compute histogram
                    NumericColumn col = (NumericColumn) table.column(x);
                    
                    double[] data = col.getValues();
                    double[] range = valueRange(data);
                    List<Double> valueList = Arrays.stream(data).boxed().collect(Collectors.toList());
                    Histogram histogram = new Histogram(valueList, 20, range[0], range[1]);
                    
                    chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Area);
                    chart.getStyler().setLegendVisible(false);
                    
                    XYSeries series = addHistogramSeries(chart, col.getName(), histogram);
                    series.setMarker(SeriesMarkers.NONE);
                    
                    charts.add(chart);
                }
                else
                {
                    // compute x-y scatter plot
                    chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
                    double[] xData = ((NumericColumn) table.column(x)).getValues();
                    double[] yData = ((NumericColumn) table.column(y)).getValues();
                    chart.addSeries("Data", xData, yData);
                    chart.getStyler().setLegendVisible(false);
                    
                    charts.add(chart);
               }
            }
        }
        
        // Show the chart matrix
        ImagoChartFrame.displayChartMatrix(frame, "Scatter Plot", charts);
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
