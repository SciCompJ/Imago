/**
 * 
 */
package imago.plugin.table;

import java.awt.Point;

import javax.swing.JFrame;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;

import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
import imago.gui.Plugin;
import net.sci.table.Table;


/**
 * Create a scatter plot from two column in a data table
 * 
 * @author David Legland
 *
 */
public class TableScatterPlot implements Plugin
{
    public TableScatterPlot()
    {
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void run(ImagoFrame frame, String args)
	{
	    // Get the data table
	    if (!(frame instanceof ImagoTableFrame))
	    {
	        return;
	    }
	    Table table = ((ImagoTableFrame) frame).getTable();

	    int col1 = 0;
	    int col2 = 1;
    
        double[] xData = table.getColumnValues(col1);
        double[] yData = table.getColumnValues(col2);
        String[] colNames = table.getColumnNames();
            
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Create Chart
                XYChart chart = new XYChartBuilder()
                        .width(600)
                        .height(500)
                        .title(table.getName())
                        .xAxisTitle(colNames[col1])
                        .yAxisTitle(colNames[col2])
                        .build();
                
                chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
                chart.getStyler().setMarkerSize(4);
                
                chart.addSeries("name", xData, yData);
                
                // Show it
                JFrame chartFrame = new SwingWrapper(chart).displayChart();
                Point pos0 = frame.getWidget().getLocation();
                chartFrame.setLocation(pos0.x + 30, pos0.y + 20);
            }
            
        });
        t.start();
	}
}