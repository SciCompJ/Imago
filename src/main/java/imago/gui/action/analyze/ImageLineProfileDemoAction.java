/**
 * 
 */
package imago.gui.action.analyze;

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoChartFrame;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.viewer.PlanarImageViewer;
import net.sci.geom.geom2d.Point2D;
import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.color.RGB8Array2D;
import net.sci.array.data.scalar2d.ScalarArray2D;
import net.sci.array.data.vector.VectorArray2D;
import net.sci.array.interp.LinearInterpolator2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.line.LineSegment2D;
import net.sci.image.Image;
import net.sci.table.DataTable;

/**
 * Simple demo for line profile that computes profile along image diagonal.
 * 
 * @author David Legland
 *
 */
public class ImageLineProfileDemoAction extends ImagoAction
{
    
    public ImageLineProfileDemoAction(ImagoFrame frame, String name)
    {
        super(frame, name);
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        // Check type is image frame
        if (!(frame instanceof ImagoDocViewer))
            return;
        ImagoDocViewer iframe = (ImagoDocViewer) frame;
        Image meta = iframe.getDocument().getImage();
        Array<?> array = meta.getData();
        
        ImageViewer viewer = iframe.getImageView();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }
        
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry2D selection = piv.getSelection();
        if (!(selection instanceof LineSegment2D))
        {
            System.out.println("requires selection to be a line segment");
            return;
        }
        
        LineSegment2D line = (LineSegment2D) selection;
        int n = 200;
        Point2D p1 = line.getP1();
        Point2D p2 = line.getP2();
        
        DataTable table = null;
        if (array instanceof ScalarArray)
        {
        	ScalarArray2D<?> array2d = ScalarArray2D.wrap((ScalarArray<?>) array);
            table = intensityProfile(array2d, p1, p2, n);
            plotIntensityProfile(table);
        }
        else if (array instanceof RGB8Array2D)
        {
            table = colorProfile((RGB8Array2D) array, p1, p2, n);
            plotRGB8LineProfile(table);
        }
        else if (array instanceof VectorArray2D)
        {
            ScalarArray2D<?> normImage = ((VectorArray2D<?>) array).norm();
            table = intensityProfile(normImage, p1, p2, n);
            plotIntensityProfile(table);
        }
        else
            throw new RuntimeException(
                    "Can not process image from class: " + array.getClass().getName());
        
    }
    
    private DataTable intensityProfile(ScalarArray2D<?> array, Point2D p1, Point2D p2, int n)
    {
        double x0 = p1.getX();
        double y0 = p1.getY();
        double dx = (p2.getX() - x0) / n;
        double dy = (p2.getY() - y0) / n;
        
        LinearInterpolator2D interp = new LinearInterpolator2D(array);
        DataTable table = new DataTable(n, 1);
        for (int i = 0; i < n; i++)
        {
            double x = x0 + i * dx;
            double y = y0 + i * dy;
            double value = interp.evaluate(x, y);
            table.setValue(i, 0, value);
        }
        
        return table;
    }
    
    private DataTable colorProfile(RGB8Array2D array, Point2D p1, Point2D p2, int n)
    {
        double x0 = p1.getX();
        double y0 = p1.getY();
        double dx = (p2.getX() - x0) / n;
        double dy = (p2.getY() - y0) / n;
        
        DataTable table = new DataTable(n, 3);
        table.setColumnNames(new String[] { "Red", "Green", "Blue" });
        
        for (int c = 0; c < 3; c++)
        {
            ScalarArray2D<?> channel = array.channel(c);
            LinearInterpolator2D interp = new LinearInterpolator2D(channel);
            for (int i = 0; i < n; i++)
            {
                double x = x0 + i * dx;
                double y = y0 + i * dy;
                double value = interp.evaluate(x, y);
                table.setValue(i, c, value);
            }
        }
        return table;
    }
    
    /**
     * Display a line profile.
     */
    private void plotIntensityProfile(DataTable profile)
    {
        int nValues = profile.getRowNumber();
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        // create a new series for each channel
        XYSeries series = new XYSeries("Intensity");
        for (int i = 0; i < nValues; i++)
        {
            series.add(i, profile.getValue(i, 0));
        }
        
        dataset.addSeries(series);
        
        // Title of the plot
        ImagoDocViewer iframe = (ImagoDocViewer) frame;
        Image image = iframe.getDocument().getImage();
        String imageName = image.getName();
        String titleString;
        if (imageName == null)
            titleString = "Intensity profile";
        else
            titleString = "Intensity profile of " + imageName;
        
        // creates the chart
        
        JFreeChart chart = ChartFactory.createXYLineChart(titleString, "Position", "Intensity",
                dataset, PlotOrientation.VERTICAL, true, true, true);
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        
        XYItemRenderer renderer = (XYItemRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        
        chart.fireChartChanged();
        
        // we put the chart into a panel
        ChartPanel chartPanel = new ChartPanel(chart, 512, 200, 512, 200, 512, 512, false, false,
                true, true, true, true);
        
        // default size
        chartPanel.setPreferredSize(new java.awt.Dimension(512, 270));
        
        chart.fireChartChanged();
        
        // creates a new frame to contains the chart panel
        ImagoChartFrame frame = new ImagoChartFrame(this.gui, "Intensity Profile");
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
    
    private void plotRGB8LineProfile(DataTable histo)
    {
        int nChannels = histo.getColumnNumber();
        int nValues = histo.getRowNumber();
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        String[] colNames = histo.getColumnNames();
        
        for (int c = 0; c < nChannels; c++)
        {
            
            // create a new series for each channel
            XYSeries series = new XYSeries(colNames[c]);
            for (int i = 0; i < nValues; i++)
            {
                series.add(i, histo.getValue(i, c));
            }
            
            // add the series to the data set
            dataset.addSeries(series);
        }
        
        // Title of the plot
        ImagoDocViewer iframe = (ImagoDocViewer) frame;
        Image image = iframe.getDocument().getImage();
        String imageName = image.getName();
        String titleString;
        if (imageName == null)
            titleString = "Color profile";
        else
            titleString = "Color profile of " + imageName;
        
        // creates the chart
        JFreeChart chart = ChartFactory.createXYLineChart(titleString, 
                "Position", // domain axis label
                "Color Intensity", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, true, true, true);
        
        // get a reference to the plot for further customisation
        XYPlot plot = chart.getXYPlot();
        
        // set the color for each series
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.BLUE);
        
        chart.fireChartChanged();
        
        // put the chart into a panel
        ChartPanel chartPanel = new ChartPanel(chart, 512, 200, 512, 200, 512, 512, false, false,
                true, true, true, true);
        
        // default size
        chartPanel.setPreferredSize(new java.awt.Dimension(512, 270));
        
        // creates a new frame to contains the chart panel
        ImagoChartFrame frame = new ImagoChartFrame(this.gui, "Color Profile");
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
        
    }
}
