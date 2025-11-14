/**
 * 
 */
package imago.image.plugin.analyze;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.markers.SeriesMarkers;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.color.RGB16Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.VectorArray;
import net.sci.array.numeric.process.Histogram;
import net.sci.image.Image;
import net.sci.image.analyze.ImageHistograms;

/**
 * @author David Legland
 *
 */
public class ImageHistogram implements FramePlugin
{
	public ImageHistogram()
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
			return;
		ImageFrame iframe = (ImageFrame) frame;
		Image image = iframe.getImageHandle().getImage();
		
		// Create the class containing histogram data
		HistogramSettings settings = new HistogramSettings(image);
		
        String name = "Histogram of " + image.getName();
		ImageHistogramDisplayFrame histoFrame = new ImageHistogramDisplayFrame(frame, name, image, settings);
		
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        try
        {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    // Display the windows.
                    histoFrame.setVisible(true);
                }
            });
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
	}
	   
    /**
     * The histogram data.
     */
    class HistogramSettings
    {
        /**
         * The range of values of image. Computed once at initialization.
         */
        double[] imageValueRange;
        
        double minValue = 0.0;
        double maxValue = 255.0;
        int nBins = 256;
        
        public HistogramSettings(Image image)
        {
            computeDefaultValueRange(image);
        }
        
        private void computeDefaultValueRange(Image image)
        {
            Array<?> array = image.getData();
            if (!(array instanceof ScalarArray)) return;
            
            if (array instanceof UInt8Array)
            {
                this.imageValueRange = new double[] {0, 255};
            }
            else
            {
                this.imageValueRange = ((ScalarArray<?>) array).finiteValueRange();
            }
            this.minValue = imageValueRange[0];
            this.maxValue = imageValueRange[1];
        }
        
    }

    /**
     * The frame that displays the histogram.
     */
    class ImageHistogramDisplayFrame extends ImagoFrame
    {
        /** the reference image, for retrieving meta-data and eventually recomputing the histogram */
        Image refImage;
        
        HistogramSettings settings;
        
        Histogram.Result[] histograms;
        String[] channelNames;
        
        boolean logScale = false;
        boolean hideBackground = false;
        boolean showLegend = true;
        
        XChartPanel<?> chartPanel;
        XYChart chart;
        
        JPanel statusBar;
        JLabel statusLabel;
        JCheckBox logScaleCheckBox;
        JCheckBox hideBackgroundCheckBox;
        JCheckBox showLegendCheckBox;
        JSpinner minValueSpinner;
        JSpinner maxValueSpinner;
        JSpinner nBinsSpinner;
        
        public ImageHistogramDisplayFrame(ImagoFrame parentFrame, String name, Image image, HistogramSettings settings) 
        {
            super(parentFrame, name);
            
            this.refImage = image;
            this.settings = settings;
            this.channelNames = computeChannelNames(image);
            
            updateHistogram();
            
            createChart();
            
            setupWidgets();
            setupLayout();
        }
        
        private String[] computeChannelNames(Image image)
        {
            return switch (image.getData())
            {
                case UInt8Array array2 -> new String[] { "Intensity" };
                case ScalarArray<?> array2 -> new String[] { "Intensity" };
                case RGB8Array array2 -> new String[] { "Red", "Green", "Blue" };
                case RGB16Array array2 -> new String[] { "Red", "Green", "Blue" };
                case VectorArray<?,?> array2 ->
                {
                    int nc = array2.channelCount();
                    String[] res = new String[nc];
                    for (int c =  0; c < nc; c++)
                    {
                        res[c] = "C" + c;
                    }
                    yield res;
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + image.getData().getClass());
            };
        }
        
        private void setupWidgets()
        {
            // Create a status bar
            this.statusBar = new JPanel();
            this.statusLabel = new JLabel("Status");
            
            this.logScaleCheckBox = new JCheckBox("Log Scale");
            this.logScaleCheckBox.addChangeListener(evt -> {
                boolean b = logScaleCheckBox.isSelected();
                if (b != logScale)
                {
                    logScale = b;
                    updateDisplay();
                }
            });
            this.hideBackgroundCheckBox = new JCheckBox("Hide Bounds");
            this.hideBackgroundCheckBox.addChangeListener(evt -> {
                boolean b = hideBackgroundCheckBox.isSelected();
                if (b != hideBackground)
                {
                    hideBackground = b;
                    updateDisplay();
                }
            });
            
            this.showLegendCheckBox = new JCheckBox("Show Legend", true);
            this.showLegendCheckBox.addChangeListener(evt -> {
                boolean b = showLegendCheckBox.isSelected();
                if (b != showLegend)
                {
                    showLegend = b;
                    chart.getStyler().setLegendVisible(showLegend);
                    this.chartPanel.repaint();
                }
            });

            if (canUpdateBounds())
            {
                this.settings.computeDefaultValueRange(this.refImage);
                
                this.minValueSpinner = new JSpinner(new SpinnerNumberModel(this.settings.minValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
                getTextField(this.minValueSpinner).setColumns(8);
                this.minValueSpinner.addChangeListener(evt -> {
                    this.settings.minValue = ((SpinnerNumberModel) minValueSpinner.getModel()).getNumber().doubleValue();
                    this.updateHistogram();
                    updateDisplay();
                });
                this.maxValueSpinner = new JSpinner(new SpinnerNumberModel(this.settings.maxValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
                getTextField(this.maxValueSpinner).setColumns(8);
                this.maxValueSpinner.addChangeListener(evt -> {
                    this.settings.maxValue = ((SpinnerNumberModel) maxValueSpinner.getModel()).getNumber().doubleValue();
                    this.updateHistogram();
                    updateDisplay();
                });
                this.nBinsSpinner = new JSpinner(new SpinnerNumberModel(this.settings.nBins, 10, 1024, 1));
                this.nBinsSpinner.addChangeListener(evt -> {
                    this.settings.nBins = ((SpinnerNumberModel) nBinsSpinner.getModel()).getNumber().intValue();
                    this.updateHistogram();
                    updateDisplay();
                });
            }
        }
        
        @SuppressWarnings({ "rawtypes", "unchecked" })
        private void setupLayout()
        {
            // add a panel containing the chart 
            chartPanel = new XChartPanel(chart);
            
            this.statusBar.add(statusLabel);
            
            // create control panel
            JPanel controlPanel = new JPanel(new FlowLayout());
            
            controlPanel.add(logScaleCheckBox);
            controlPanel.add(hideBackgroundCheckBox);
            controlPanel.add(showLegendCheckBox);
            
            if (canUpdateBounds())
            {
                controlPanel.add(new JLabel("Min:"));
                controlPanel.add(minValueSpinner);
                controlPanel.add(new JLabel("Max:"));
                controlPanel.add(maxValueSpinner);
                controlPanel.add(new JLabel("NBins"));
                controlPanel.add(nBinsSpinner);
            }
            
            JPanel middlePanel = new JPanel(new BorderLayout());
            middlePanel.add(chartPanel, BorderLayout.CENTER);
            middlePanel.add(statusBar, BorderLayout.SOUTH);
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(middlePanel, BorderLayout.CENTER);
            mainPanel.add(controlPanel, BorderLayout.SOUTH);
            
            jFrame.add(mainPanel);
            jFrame.pack();            
        }
        
        private void createChart()
        {
            int nChannels = this.histograms.length;

            // Create Chart
            this.chart = new XYChartBuilder()
                    .width(600)
                    .height(500)
                    .xAxisTitle("Value")
                    .yAxisTitle("Count")
                    .build();

            // Additional chart style
            chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
            chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
            
            // create curve for each channel
            XYSeries[] series  = new XYSeries[nChannels];
            for (int c = 0; c < nChannels; c++)
            {
                Histogram.Result histo = this.histograms[c];
                double[] data = convertToDouble(histo.counts());
                series[c] = chart.addSeries(channelNames[c], histo.binCenters(), data);
                series[c].setMarker(SeriesMarkers.NONE);
            }

            // changes default colors of color histograms
            if (nChannels == 3)
            {
                series[0].setLineColor(XChartSeriesColors.RED);
                series[1].setLineColor(XChartSeriesColors.GREEN);
                series[2].setLineColor(XChartSeriesColors.BLUE);
            }
        }
        
        private void updateHistogram()
        {
//            histogramData = settings.update();
            Array<?> array = this.refImage.getData();
            
            histograms = switch(array)
            {
                case UInt8Array array2  -> computeHistogramData_UInt8(array2);
                case RGB8Array array2   -> computeHistogramData_RGB8(array2);
                case RGB16Array array2  -> computeHistogramData_RGB16(array2);
                case ScalarArray<?> array2 -> computeHistogramData_Scalar(array2);
                default -> throw new RuntimeException(
                        "Unable to compute histogram for array class: " + array.getClass());
            };
        }
        
        private Histogram.Result[] computeHistogramData_UInt8(UInt8Array array)
        {
            return new Histogram.Result[] {ImageHistograms.histogramUInt8(array)};
        }
        
        private Histogram.Result[] computeHistogramData_Scalar(ScalarArray<?> array)
        {
            Histogram algo = new Histogram(settings.imageValueRange, settings.nBins);
            return new Histogram.Result[] {algo.process(array)};
        }
        
        private Histogram.Result[] computeHistogramData_RGB8(RGB8Array array)
        {
            return ImageHistograms.histogramsRGB8(array);
        }

        private Histogram.Result[] computeHistogramData_RGB16(RGB16Array array)
        { 
            return ImageHistograms.histogramsRGB16(array);
        }

        public void updateDisplay()
        {
            Map<String, XYSeries> map = chart.getSeriesMap();
            for (int c = 0; c < this.histograms.length; c++)
            {
                String name = this.channelNames[c];
                XYSeries series = map.get(name);
                
                // check log scale
                double[] data = convertToDouble(this.histograms[c].counts());
                if (logScale)
                {
                    for (int i = 0; i < data.length; i++)
                    {
                        data[i] = Math.log(data[i] + 1);
                    }
                    this.chart.setYAxisTitle("Log Count");
                }
                else
                {
                    this.chart.setYAxisTitle("Count");
                }
                
                // check display of histogram bound bins
                if (hideBackground)
                {
                    data[0] = Double.NaN;
                    data[data.length-1] = Double.NaN;
                }
                
                series.replaceData(this.histograms[c].binCenters(), data, null);
            }
            
            this.chartPanel.repaint();
        }
        
        /**
         * Check if it is possible to update bounds of the histogram. Bounds can
         * be update for scalar array that are not instances of UInt8Array.
         */
        public boolean canUpdateBounds()
        {
            if (this.refImage.getData() instanceof UInt8Array)
            {
                return false;
            }
            return this.refImage.isScalarImage();
        }
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

    private static final JFormattedTextField getTextField(JSpinner spinner)
    {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor)
        {
            return ((JSpinner.DefaultEditor) editor).getTextField();
        }
        else
        {
            System.err.println("Unexpected editor type: " + spinner.getEditor().getClass() + " isn't a descendant of DefaultEditor");
            return null;
        }
    }   
}
