/**
 * 
 */
package imago.plugin.image.analyze;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoChartFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.PlanarImageViewer;
import imago.gui.image.StackSliceViewer;
import imago.plugin.image.ImagePlugin;
import net.sci.array.Array;
import net.sci.array.Array2D;
import net.sci.array.Array3D;
import net.sci.array.color.RGB8Array2D;
import net.sci.array.interp.LinearInterpolator2D;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.vector.VectorArray2D;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;
import net.sci.table.DefaultNumericTable;

/**
 * Simple demo for line profile that computes profile along image diagonal.
 * 
 * @author David Legland
 *
 */
public class ImageLineProfile implements ImagePlugin
{
    public ImageLineProfile()
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
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
        {
            return;
        }
        ImageFrame iframe = (ImageFrame) frame;
        
        // retrieve inner data
        Image meta = iframe.getImageHandle().getImage();
        Array<?> array = meta.getData();
        
        // the data to combine for creating the profile
        Array2D<?> array2d;
        Geometry2D selection;
        
        // check viewer is planar
        ImageViewer viewer = iframe.getImageViewer();
        if (viewer instanceof PlanarImageViewer)
        {
            PlanarImageViewer piv = (PlanarImageViewer) viewer;
            array2d = (Array2D<?>) array;
            selection = piv.getSelection();
        }
        else if (viewer instanceof StackSliceViewer)
        {
            int zSlice = ((StackSliceViewer) viewer).getSlicingPosition(2);
            array2d = Array3D.wrap(array).slice(zSlice);
            Geometry sel = ((StackSliceViewer) viewer).getSelection();
            if (!(sel instanceof Geometry2D))
            {
                System.out.println("Selection must be an instance of Geometry2D");
                return;
            }
            selection = (Geometry2D) sel;
        }
        else
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }

        // retrieve current selection
        if (!(selection instanceof LineSegment2D))
        {
            System.out.println("requires selection to be a line segment");
            return;
        }
        LineSegment2D line = (LineSegment2D) selection;
        
        processLineProfile_2d(iframe, array2d, line);
    }
    
    private void processLineProfile_2d(ImageFrame iframe, Array2D<?> array, LineSegment2D line)
    {
        // get line extremities
        int nPositions = (int) Math.round(line.length());
        Point2D p1 = line.getP1();
        Point2D p2 = line.getP2();
        
        // dispatch processing according to image type
        DefaultNumericTable table = null;
        if (array instanceof ScalarArray)
        {
            ScalarArray2D<?> array2d = ScalarArray2D.wrap((ScalarArray<?>) array);
            table = intensityProfile(array2d, p1, p2, nPositions);
            plotIntensityProfile(iframe, table);
        }
        else if (array instanceof RGB8Array2D)
        {
            table = colorProfile((RGB8Array2D) array, p1, p2, nPositions);
            plotRGB8LineProfile(iframe, table);
        }
        else if (array instanceof VectorArray2D)
        {
            ScalarArray2D<?> normImage = ((VectorArray2D<?,?>) array).norm();
            table = intensityProfile(normImage, p1, p2, nPositions);
            plotIntensityProfile(iframe, table);
        }
        else
            throw new RuntimeException(
                    "Can not process image from class: " + array.getClass().getName());
    }
    
    
    private DefaultNumericTable intensityProfile(ScalarArray2D<?> array, Point2D p1, Point2D p2, int n)
    {
        double x0 = p1.x();
        double y0 = p1.y();
        double dx = (p2.x() - x0) / n;
        double dy = (p2.y() - y0) / n;
        
        LinearInterpolator2D interp = new LinearInterpolator2D(array);
        DefaultNumericTable table = new DefaultNumericTable(n, 1);
        for (int i = 0; i < n; i++)
        {
            double x = x0 + i * dx;
            double y = y0 + i * dy;
            double value = interp.evaluate(x, y);
            table.setValue(i, 0, value);
        }
        
        return table;
    }
    
    private DefaultNumericTable colorProfile(RGB8Array2D array, Point2D p1, Point2D p2, int n)
    {
        double x0 = p1.x();
        double y0 = p1.y();
        double dx = (p2.x() - x0) / n;
        double dy = (p2.y() - y0) / n;
        
        DefaultNumericTable table = new DefaultNumericTable(n, 3);
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
    private void plotIntensityProfile(ImagoFrame parentFrame, DefaultNumericTable table)
    {
        int nValues = table.rowCount();

        // Default name for table
        String tableName = table.getName();
        if (tableName == null || tableName.length() == 0)
        {
            tableName = "data";
        }

        // Title of the plot
        ImageFrame iframe = (ImageFrame) parentFrame;
        Image image = iframe.getImageHandle().getImage();
        String titleString = createTitleString("Intensity profile", image.getName());

        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(titleString)
                .xAxisTitle("Position [pixels]")
                .yAxisTitle("Intensity")
                .build();
        
        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        
        // create series and series style
        double[] xData = generateLinearVector(nValues, 0);
        XYSeries series = chart.addSeries("Intensity", xData, table.getColumnValues(0));
        series.setMarker(SeriesMarkers.NONE);
        
        // Show it
        ImagoChartFrame.displayChart(parentFrame, "Intensity Profile", chart);
    }
    
    private void plotRGB8LineProfile(ImagoFrame parentFrame, DefaultNumericTable table)
    {
        int nChannels = table.columnCount();
        int nValues = table.rowCount();
        String[] colNames = table.getColumnNames();
        
        // Default name for table
        String tableName = table.getName();
        if (tableName == null || tableName.length() == 0)
        {
            tableName = "data";
        }

        // Title of the plot
        ImageFrame iframe = (ImageFrame) parentFrame;
        Image image = iframe.getImageHandle().getImage();
        String titleString = createTitleString("Color profile", image.getName());
        
        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(titleString)
                .xAxisTitle("Position")
                .yAxisTitle("Intensity")
                .build();
        
        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        
        // create a new series for each channel
        double[] xData = generateLinearVector(nValues, 0);
        XYSeries[] series  = new XYSeries[nChannels];
        for (int c = 0; c < nChannels; c++)
        {
            series[c] = chart.addSeries(colNames[c], xData, table.getColumnValues(c));
            series[c].setMarker(SeriesMarkers.NONE);
        }
        
        // changes default colors
        series[0].setLineColor(XChartSeriesColors.RED);
        series[1].setLineColor(XChartSeriesColors.GREEN);
        series[2].setLineColor(XChartSeriesColors.BLUE);
        
        // Show it
        ImagoChartFrame.displayChart(parentFrame, "Color Profile", chart);
    }
    
    private String createTitleString(String baseTitle, String imageName)
    {
        if (imageName != null)
        {
            baseTitle += " of " + imageName;
        }
        return baseTitle;
    }
    
    /**
     * Generate a linear vectors containing values starting from 1, 2... to
     * nValues.
     * 
     * @param nValues
     *            the number of values
     * @return a linear vector of nRows values
     */
    private double[] generateLinearVector(int nValues, double startValue)
    {
        double[] values = new double[nValues];
        for (int i = 0; i < nValues; i++)
        {
            values[i] = startValue + i;
        }
        return values;
    }
    
}
