/**
 * 
 */
package imago.plugin.image.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.frames.ImagoChartFrame;
import imago.gui.FramePlugin;
import imago.gui.viewer.PlanarImageViewer;
import net.sci.array.Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.vector.VectorArray;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;

/**
 * @author dlegland
 *
 */
public class ImagePlotChannels implements FramePlugin
{
    
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        Image meta = iframe.getImageHandle().getImage();
        
        Array<?> array = meta.getData();
        if (!(array instanceof VectorArray))
        {
            throw new RuntimeException("Input array must be a vector array");
        }
        VectorArray<?> vectorArray = (VectorArray<?>) array;
        
        ImageViewer viewer = iframe.getImageView();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("Requires an instance of planar image viewer");
            return;
        }
        
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry2D selection = piv.getSelection();
        if (selection == null)
        {
            throw new RuntimeException("Requires a selection");
        }
        Point2D point = computeReferencePoint(selection);
        
//        point = piv.getImageDisplay().displayToImage(point);
        double[] values = vectorArray.getValues(new int[] {(int) point.getX(), (int) point.getY()});
        
        if (vectorArray instanceof RGB8Array)
        {
            plotRGBProfile(frame, values);
        }
        else
        {
            plotChannelsProfile(frame, values);
        }
    }
    
    private Point2D computeReferencePoint(Geometry2D geom)
    {
        if (geom instanceof Point2D)
            return (Point2D) geom;
        if (geom instanceof LineSegment2D)
            return ((LineSegment2D) geom).getP1();
       
        throw new RuntimeException("Unable to determine a reference point from selection with type: " + geom.getClass().getName());
    }
    
    /**
     * Display a profiles of values along channels.
     */
    private void plotChannelsProfile(ImagoFrame parentFrame, double[] values)
    {
        int nValues = values.length;

        // Title of the plot
        ImageFrame iframe = (ImageFrame) parentFrame;
        Image image = iframe.getImageHandle().getImage();
        String titleString = createTitleString("Channel profile", image.getName());

        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(titleString)
                .xAxisTitle("Channel")
                .yAxisTitle("Intensity")
                .build();
        
        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        
        // create series and series style
        double[] xData = generateLinearVector(nValues, 0);
        XYSeries series = chart.addSeries("Intensity", xData, values);
        series.setMarker(SeriesMarkers.NONE);
        
        // Show it
        ImagoChartFrame.displayChart(parentFrame, "Intensity Profile", chart);
    }
    
    /**
     * Display a profiles of values along channels.
     */
    private void plotRGBProfile(ImagoFrame parentFrame, double[] values)
    {
        int nValues = values.length;

        // Title of the plot
        ImageFrame iframe = (ImageFrame) parentFrame;
        Image image = iframe.getImageHandle().getImage();
        String titleString = createTitleString("Channel profile", image.getName());

        
        // Create Chart
        CategoryChart chart = new CategoryChartBuilder()
                .width(600)
                .height(500)
                .title(titleString)
                .xAxisTitle("Channel")
                .yAxisTitle("Intensity")
                //.theme(ChartTheme.GGPlot2)
                .build();
     
        // Customize Chart
     
        // Series
        List<String> channelNames = Arrays.asList(new String[] { "Red", "Green", "Blue" });
        List<Double> valueList = new ArrayList<Double>(nValues);
        for (double v : values)
        {
            valueList.add(Double.valueOf(v));
        }
        chart.addSeries("values", channelNames, valueList);
       
        // Show it
        ImagoChartFrame.displayChart(parentFrame, "RGB values", chart);
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
