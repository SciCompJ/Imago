/**
 * 
 */
package imago.plugin.table.plot;

import java.util.ArrayList;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.chart.ChartFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.table.CategoricalColumn;
import net.sci.table.NumericColumn;
import net.sci.table.Table;


/**
 * Create a scatter plot from two columns in a data table, using a third column
 * to determine groups.
 * 
 * @see TableScatterPlot
 * 
 * @author David Legland
 */
public class TableGroupScatterPlot implements TablePlugin
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
        
        if (table.columnCount() < 3)
        {
            throw new RuntimeException("Requires a table with at least three columns");
        }
        
        int indColX = 0;
        int indColY = 1;
        int indColG = 2;
        GenericDialog dlg = new GenericDialog(frame, "Scatter Groups");
        String[] colNames = table.getColumnNames();
        dlg.addChoice("X-Axis Column", colNames, colNames[0]);
        dlg.addChoice("Y-Axis Column", colNames, colNames[1]);
        dlg.addChoice("Groups Column", colNames, colNames[2]);
        
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        indColX = dlg.getNextChoiceIndex();
        indColY = dlg.getNextChoiceIndex();
        indColG = dlg.getNextChoiceIndex();
   
        if (!(table.column(indColX) instanceof NumericColumn))
        {
            throw new RuntimeException("Requires a numeric column");
        }
        if (!(table.column(indColY) instanceof NumericColumn))
        {
            throw new RuntimeException("Requires a numeric column");
        }
        if (!(table.column(indColG) instanceof CategoricalColumn))
        {
            throw new RuntimeException("Group column must be a categorical column");
        }
        NumericColumn colX = (NumericColumn) table.column(indColX);
        NumericColumn colY = (NumericColumn) table.column(indColY);
        CategoricalColumn groups = (CategoricalColumn) table.column(indColG);
        
        int nGroups = groups.levelNames().length;
        ArrayList<ArrayList<Double>> xData = new ArrayList<>(nGroups);
        ArrayList<ArrayList<Double>> yData = new ArrayList<>(nGroups);
        for (int i = 0; i < nGroups; i++)
        {
            xData.add(new ArrayList<Double>());
            yData.add(new ArrayList<Double>());
        }
        
        // iterate over rows
        for (int i = 0; i < table.rowCount(); i++)
        {
            int iGroup = groups.getLevelIndex(i);
            xData.get(iGroup).add(colX.getValue(i));
            yData.get(iGroup).add(colY.getValue(i));
        }

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
        chart.getStyler().setLegendVisible(true);
        
        String[] levelNames = groups.levelNames();
        for (int i = 0; i < nGroups; i++)
        {
            double[] xarr = xData.get(i).stream().mapToDouble(Double::doubleValue).toArray();
            double[] yarr = yData.get(i).stream().mapToDouble(Double::doubleValue).toArray();
            chart.addSeries(levelNames[i], xarr, yarr);
        }
        
        // Show it
        ChartFrame.create(chart, "Scatter Plot", frame);
	}
}
