/**
 * 
 */
package imago.plugin.table.plot;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.gui.GenericDialog;
import imago.gui.ImagoChartFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.table.Table;


/**
 * Create a line plot from one or more column(s) in a data table
 * 
 * @author David Legland
 *
 */
public class TableLinePlot implements TablePlugin
{
    public TableLinePlot()
    {
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
    @Override
    public void run(ImagoFrame frame, String args)
	{
	    // Get the data table
	    if (!(frame instanceof ImagoTableFrame))
	    {
	        return;
	    }
	    Table table = ((ImagoTableFrame) frame).getTable();

	    // get general info from table
	    int nRows = table.getRowNumber();
	    int nCols = table.getColumnNumber();
        String[] colNames = table.getColumnNames();
        
        String[] colNames2 = new String[nCols + 1];
        colNames2[0] = "none";
        System.arraycopy(colNames, 0, colNames2, 1, nCols);
 
        // Display dialog for choosing options
	    GenericDialog dlg = new GenericDialog(frame, "Line Plot");
	    dlg.addChoice("X-Axis", colNames2, colNames2[0]);

	    int nCols2 = Math.min(nCols, 10);
	    for (int i = 0; i < nCols2; i++)
	    {
	        dlg.addCheckBox(colNames[i], false);
	    }
	    dlg.showDialog();
        
        if (dlg.wasCanceled()) 
        {
            return;
        }
	    
        // Parse dialog contents
        int xAxisIndex = dlg.getNextChoiceIndex();
        boolean[] showColumnFlags = new boolean[nCols2];
        for (int i = 0; i < nCols2; i++)
        {
            showColumnFlags[i] = dlg.getNextBoolean();
        }
        
        // Choose data for x-axis, or generate if requested
	    double[] xData = xAxisIndex == 0 ? generateLinearVector(nRows) : table.getColumnValues(xAxisIndex-1);

	    // Default name for table
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
                .xAxisTitle("obs. index")
                .yAxisTitle("")
                .build();
        
        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        
        // create series and series style
        for (int i = 0; i < nCols2; i++)
        {
            if (showColumnFlags[i]) 
            {
                XYSeries series = chart.addSeries(colNames[i], xData, table.getColumnValues(i));
                series.setMarker(SeriesMarkers.NONE);
            }
        }

        // Show it
        ImagoChartFrame.displayChart(frame, "Line Plot", chart);
	}
	
	/**
     * Generate a linear vectors containing values starting from 1, 2... to
     * nRows.
     * 
     * @param nRows
     *            the number of values
     * @return a linear vector of nRows values
     */
	private double[] generateLinearVector(int nRows)
	{
	    double[] values = new double[nRows];
	    for (int i = 0; i < nRows; i++)
	    {
	        values[i] = i+1;
	    }
	    return values;
	}
}
