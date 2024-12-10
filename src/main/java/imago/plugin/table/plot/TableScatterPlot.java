/**
 * 
 */
package imago.plugin.table.plot;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.chart.ChartFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.table.NumericColumn;
import net.sci.table.Table;


/**
 * Create a scatter plot from two (numeric) columns in a data table.
 * 
 * @see TableGroupScatterPlot
 * 
 * @author David Legland
 */
public class TableScatterPlot implements TablePlugin
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
        
        if (table.columnCount() < 2)
        {
            throw new RuntimeException("Requires a table with at least two columns");
        }
        
        int indColX = 0;
        int indColY = 1;
        GenericDialog dlg = new GenericDialog(frame, "Scatter Plot");
        String[] colNames = table.getColumnNames();
        dlg.addChoice("X-Axis Column", colNames, colNames[0]);
        dlg.addChoice("Y-Axis Column", colNames, colNames[1]);
        
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        indColX = dlg.getNextChoiceIndex();
        indColY = dlg.getNextChoiceIndex();
   
        if (!(table.column(indColX) instanceof NumericColumn))
        {
            throw new RuntimeException("Requires a numeric column");
        }
        if (!(table.column(indColY) instanceof NumericColumn))
        {
            throw new RuntimeException("Requires a numeric column");
        }
        NumericColumn colX = (NumericColumn) table.column(indColX);
        NumericColumn colY = (NumericColumn) table.column(indColY);
        
        double[] xData = colX.getValues();
        double[] yData = colY.getValues();

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
                .xAxisTitle(colNames[indColX])
                .yAxisTitle(colNames[indColY])
                .theme(ChartTheme.Matlab)
                .build();
        
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        chart.getStyler().setMarkerSize(4);
        
        chart.addSeries("data", xData, yData);
        chart.getStyler().setLegendVisible(false);
        
        // Show it
        ChartFrame.create(chart, "Scatter Plot", frame);
	}
}
