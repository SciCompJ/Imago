/**
 * 
 */
package imago.table.plugin.plot;

import java.awt.Color;
import java.util.ArrayList;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.chart.ChartFrame;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.plugin.TableFramePlugin;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
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
public class TableGroupScatterPlot implements TableFramePlugin
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
        dlg.addCheckBox("Show ellipses", true);
        
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        indColX = dlg.getNextChoiceIndex();
        indColY = dlg.getNextChoiceIndex();
        indColG = dlg.getNextChoiceIndex();
        boolean showEllipses = dlg.getNextBoolean();
   
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
        
        // a series of colors for markers
        Color[] colors = new Color[] {Color.RED, new Color(0, 128, 0), Color.BLUE, new Color(128, 0, 0), new Color(0, 128, 0), new Color(0, 0, 128), new Color(128, 128, 0)};
        
        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(chartTitle)
                .xAxisTitle(colNames[indColX])
                .yAxisTitle(colNames[indColY])
                .build();
        
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        chart.getStyler().setLegendVisible(true);
        
        String[] levelNames = groups.levelNames();
        for (int i = 0; i < nGroups; i++)
        {
            double[] xarr = xData.get(i).stream().mapToDouble(Double::doubleValue).toArray();
            double[] yarr = yData.get(i).stream().mapToDouble(Double::doubleValue).toArray();
            Color color = colors[i % colors.length];
            
            if (xarr.length > 0)
            {
                XYSeries scatterSeries = chart.addSeries(levelNames[i], xarr, yarr);
                scatterSeries.setMarkerColor(color);
                
                if (showEllipses)
                {
                    Ellipse2D elli = Ellipse2D.equivalentEllipse(xarr, yarr);
                    double[] xElli = new double[63];
                    double[] yElli = new double[63];
                    fillCoords(elli, xElli, yElli);
                    
                    XYSeries eqElli = chart.addSeries(levelNames[i]+"-elli", xElli, yElli);
                    eqElli.setLineColor(color);
                    eqElli.setXYSeriesRenderStyle(XYSeriesRenderStyle.Line);
                    eqElli.setMarker(SeriesMarkers.NONE);
                    eqElli.setShowInLegend(false);
                }
            }
        }
        
        // Show it
        ChartFrame.create(chart, "Scatter Plot", frame);
	}
    
    private static final void fillCoords(Ellipse2D elli, double[] xData, double[] yData)
    {
        int np = xData.length;
        double dt = (elli.t1() - elli.t0()) / (np - 1);
        for (int i = 0; i < np; i++)
        {
            Point2D p = elli.point(i * dt);
            xData[i] = p.x();
            yData[i] = p.y();
        }
    }
}
