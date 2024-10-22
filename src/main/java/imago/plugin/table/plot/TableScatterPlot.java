/**
 * 
 */
package imago.plugin.table.plot;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoChartFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.table.NumericColumn;
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
        
        if (table.columnCount() < 2)
        {
            throw new RuntimeException("Requires a table with at least two columns");
        }
        
        int indCol1 = 0;
        int indCol2 = 1;
        GenericDialog dlg = new GenericDialog(frame, "Scatter Plot");
        String[] colNames = table.getColumnNames();
        dlg.addChoice("X-Axis Column", colNames, colNames[0]);
        dlg.addChoice("Y-Axis Column", colNames, colNames[1]);
        
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        indCol1 = dlg.getNextChoiceIndex();
        indCol2 = dlg.getNextChoiceIndex();
   
        if (!(table.column(indCol1) instanceof NumericColumn))
        {
            throw new RuntimeException("Requires a numeric column");
        }
        if (!(table.column(indCol2) instanceof NumericColumn))
        {
            throw new RuntimeException("Requires a numeric column");
        }
        NumericColumn col1 = (NumericColumn) table.column(indCol1);
        NumericColumn col2 = (NumericColumn) table.column(indCol2);
        
        double[] xData = col1.getValues();
        double[] yData = col2.getValues();

        String chartTitle = table.getName();
        if (chartTitle == null || chartTitle.length() == 0)
        {
            chartTitle = "data";
        }
        
        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(chartTitle)
                .xAxisTitle(colNames[indCol1])
                .yAxisTitle(colNames[indCol2])
                .build();
        
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        chart.getStyler().setMarkerSize(4);
        
        chart.addSeries("data", xData, yData);
        chart.getStyler().setLegendVisible(false);
        
        // Show it
        ImagoChartFrame.displayChart(frame, "Scatter Plot", chart);
	}
}
