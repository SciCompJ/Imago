/**
 * 
 */
package imago.plugin.table.plot;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;

import imago.gui.ImagoChartFrame;
import imago.gui.ImagoFrame;
import imago.gui.TableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.table.Table;


/**
 * Create a scatter plot from two column in a data table
 * 
 * @author David Legland
 *
 */
public class TableScatterPlot implements TablePlugin
{
    public TableScatterPlot()
    {
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
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

	    int col1 = 0;
	    int col2 = 1;
    
        double[] xData = table.getColumnValues(col1);
        double[] yData = table.getColumnValues(col2);
        String[] colNames = table.getColumnNames();
            
        String tableName = table.getName();
        if (tableName == null || tableName.length() == 0)
        {
            tableName = "data";
        }
        
        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(tableName)
                .xAxisTitle(colNames[col1])
                .yAxisTitle(colNames[col2])
                .build();
        
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        chart.getStyler().setMarkerSize(4);
        
        chart.addSeries("data", xData, yData);
        chart.getStyler().setLegendVisible(false);
        
        // Show it
        ImagoChartFrame.displayChart(frame, "Scatter Plot", chart);
	}
}
