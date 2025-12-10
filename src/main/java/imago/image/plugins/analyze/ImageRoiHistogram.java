/**
 * 
 */
package imago.image.plugins.analyze;

//import java.awt.Color;

//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.StandardXYBarPainter;
//import org.jfree.chart.renderer.xy.XYBarRenderer;
//import org.jfree.chart.renderer.xy.XYItemRenderer;
//import org.jfree.data.statistics.SimpleHistogramBin;
//import org.jfree.data.statistics.SimpleHistogramDataset;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.gui.ImagoFrame;
//import imago.gui.frames.ImagoChartFrame;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.image.viewers.PlanarImageViewer;
import imago.chart.ChartFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.color.RGB8Array2D;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.process.Histogram;
import net.sci.geom.geom2d.Domain2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;
import net.sci.image.analyze.ImageHistograms;

/**
 * Computes and display histogram within current (2D) ROI.
 * 
 * @author David Legland
 *
 */
public class ImageRoiHistogram implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageRoiHistogram()
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
        if (!(frame instanceof ImageFrame)) return;
        ImageFrame iframe = (ImageFrame) frame;
        Image image = iframe.getImageHandle().getImage();

        ImageViewer viewer = iframe.getImageViewer();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }

        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry2D selection = piv.getSelection();
        if (!(selection instanceof Domain2D))
        {
            System.out.println("requires selection to be a planar domain");
            return;
        }

        Histogram.Result[] histos = computeHistograms(image, (Domain2D) selection);
        showHistogram(frame, histos);
    }

    private Histogram.Result[] computeHistograms(Image image, Domain2D domain)
    {
        Array<?> array = image.getData();
        if (array instanceof ScalarArray2D)
        {
            double[] range = image.getDisplaySettings().getDisplayRange();
            System.out.println(
                    String.format("Display range for histogram: (%f ; %f)", range[0], range[1]));

            Histogram.Result histo = ImageHistograms.histogramScalar((ScalarArray2D<?>) array,
                    domain, range, 256);
            return new Histogram.Result[] { histo };
        }
        else if (array instanceof RGB8Array2D)
        {
            return ImageHistograms.histogramsRGB8((RGB8Array2D) array, domain);
        }
        else
        {
            throw new RuntimeException(
                    "Unable to compute histogram for array class: " + array.getClass());
        }
    }

    private void showHistogram(ImagoFrame parentFrame, Histogram.Result[] histos)
    {
        switch (histos.length)
        {
            case 1 -> showGray8Histogram(parentFrame, histos[0]);
            case 3 -> showRGB8Histogram(parentFrame, histos);
            default -> throw new RuntimeException(
                    "Unable to display histogram of images with " + histos.length + " columns");
        }
    }

    /**
     * Display histogram of 256 gray scale images.
     */
    private void showGray8Histogram(ImagoFrame parentFrame, Histogram.Result histo)
    {
        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .xAxisTitle("Value")
                .yAxisTitle("Count")
                .build();

        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);

        // create curve for intensity channel
        double[] data = convertToDouble(histo.counts());
        XYSeries series = chart.addSeries("Intensity", histo.binCenters(), data);
        series.setMarker(SeriesMarkers.NONE);

        // Show it
        ChartFrame.create(chart, "Histogram", parentFrame);
    }

    private void showRGB8Histogram(ImagoFrame parentFrame, Histogram.Result[] histos)
    {
        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .xAxisTitle("Value")
                .yAxisTitle("Count")
                .build();

        // Additional chart style
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);

        // create curve for each channel
        int nChannels = histos.length;
        String[] channelNames = new String[] { "Red", "Green", "Blue" };
        XYSeries[] series = new XYSeries[nChannels];
        for (int c = 0; c < nChannels; c++)
        {
            Histogram.Result histo = histos[c];
            double[] data = convertToDouble(histo.counts());
            series[c] = chart.addSeries(channelNames[c], histo.binCenters(), data);
            series[c].setMarker(SeriesMarkers.NONE);
        }

        // changes default colors of color histograms
        series[0].setLineColor(XChartSeriesColors.RED);
        series[1].setLineColor(XChartSeriesColors.GREEN);
        series[2].setLineColor(XChartSeriesColors.BLUE);

        // Show it
        ChartFrame.create(chart, "Histogram", parentFrame);
    }

    private static final double[] convertToDouble(int[] array)
    {
        double[] res = new double[array.length];
        for (int i = 0; i < array.length; i++)
        {
            res[i] = array[i];
        }
        return res;
    }
}
