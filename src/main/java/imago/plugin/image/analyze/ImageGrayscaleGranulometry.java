/**
 * 
 */
package imago.plugin.image.analyze;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.chart.ChartFrame;
import imago.gui.image.ImageFrame;
import imago.gui.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.image.Image;
import net.sci.image.analyze.texture.GrayscaleGranulometry;
import net.sci.image.analyze.texture.GrayscaleGranulometry.Type;
import net.sci.image.morphology.strel.Strel2D;
import net.sci.table.NumericTable;

/**
 * Computation of gray-level granulometry from scalar images based on
 * mathematical morphology operators.
 */
public class ImageGrayscaleGranulometry implements FramePlugin
{
    /**
     * Default empty constructor
     */
    public ImageGrayscaleGranulometry()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        // check input data type
        if (!(array instanceof ScalarArray2D))
        {
            return;
        }
        
        // Create dialog for setting options
        GenericDialog gd = new GenericDialog(frame, "Grayscale Granulometry");
        gd.addChoice("Operation", GrayscaleGranulometry.Type.all(), GrayscaleGranulometry.Type.CLOSING);
        gd.addChoice("Element", Strel2D.Shape.getAllLabels(), 
                Strel2D.Shape.SQUARE.toString());
        gd.addNumericField("Radius Max. (in pixels)", 25, 0);
        gd.addNumericField("Step (in pixels)", 1, 0);
        
        gd.showDialog();
        if (gd.wasCanceled()) 
        {
            return;
        }
        
        // parse user choices
        Type type           = Type.fromLabel(gd.getNextChoice());
        Strel2D.Shape shape = Strel2D.Shape.fromLabel(gd.getNextChoice());
        int radiusMax       = (int) gd.getNextNumber();     
        int step            = (int) gd.getNextNumber();
        
        // create computation class
        GrayscaleGranulometry algo = new GrayscaleGranulometry()
                .type(type)
                .strelShape(shape)
                .radiusMax(radiusMax)
                .radiusStep(step);
        algo.addAlgoListener(imageFrame);
        
        // Compute granulometry curve
        NumericTable table = algo.granulometryCurve((ScalarArray2D<?>) array);

        
        // add the new frame to the GUI
        TableFrame.create(table, frame);

        plotGranulometryCurve(frame, table);
    }
    
    /**
     * Display a line profile.
     */
    private void plotGranulometryCurve(ImagoFrame parentFrame, NumericTable table)
    {
        // Default name for table
        String tableName = table.getName();
        if (tableName == null || tableName.length() == 0)
        {
            tableName = "Granulometry";
        }

        // Title of the plot
        ImageFrame iframe = (ImageFrame) parentFrame;
        Image image = iframe.getImageHandle().getImage();
        String titleString = createTitleString("Granulometry", image.getName());

        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .title(titleString)
                .xAxisTitle("Diameter (pixels)")
                .yAxisTitle("Graylevel variation (%)")
                .build();
        
        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        
        // create series and series style
        double[] xData = table.getColumnValues(0);
        double[] yData = table.getColumnValues(1);
        String seriesName = image.getName();
        if (seriesName == null || seriesName.isEmpty()) seriesName = "Granulometry Curve";
        XYSeries series = chart.addSeries(seriesName, xData, yData);
        series.setMarker(SeriesMarkers.NONE);
        
        // Show it
        ChartFrame.create(chart, titleString, parentFrame);
    }
    
    private static final String createTitleString(String baseTitle, String imageName)
    {
        if (imageName != null)
        {
            baseTitle += " of " + imageName;
        }
        return baseTitle;
    }
    

}
