/**
 * 
 */
package imago.table.plugins.plot;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.chart.ChartFrame;
import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.plugins.TableFramePlugin;
import net.sci.axis.Axis;
import net.sci.axis.NumericalAxis;
import net.sci.table.NumericTable;
import net.sci.table.Table;


/**
 * Create a line plot of the rows within a (numeric) table.
 * 
 * @author David Legland
 *
 */
public class PlotTableRows implements TableFramePlugin
{
    /**
     * Default empty constructor.
     */
    public PlotTableRows()
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
        // Get the data table
        if (!(frame instanceof TableFrame))
        {
            return;
        }
        Table table = ((TableFrame) frame).getTable();

        if (!(table instanceof NumericTable))
        {
            System.err.println("Requires a numeric table");
            return;
        }
        NumericTable numTab = (NumericTable) table;
        
        // get general info from table
        int nRows = table.rowCount();
        int nCols = table.columnCount();
        String[] rowNames = table.getRowNames();
        
        // Default name for table
        String tableName = table.getName();
        if (tableName == null || tableName.length() == 0)
        {
            tableName = "data";
        }
        
        // Generate data for x-axis
        double[] xData = new double[nCols];
        Axis colAxis = numTab.getColumnAxis();
        if (colAxis instanceof NumericalAxis numAx)
        {
            for (int c = 0; c < nCols; c++)
            {
                xData[c] = numAx.getValue(c);
            }
        }
        else
        {
            for (int c = 0; c < nCols; c++)
            {
                xData[c] = c + 1;
            }
        }
        String xAxisName = colAxis.getName();

        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(tableName)
                .xAxisTitle(xAxisName)
                .yAxisTitle("")
                .build();
        
        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        
        // create series and series style
        for (int i = 0; i < nRows; i++)
        {
            // add a new series with data from current row
            XYSeries series = chart.addSeries(rowNames[i], xData, numTab.getRowValues(i));
            series.setMarker(SeriesMarkers.NONE);
        }

        // Show it
        ChartFrame.create(chart, "Line Plot", frame);
    }
}
