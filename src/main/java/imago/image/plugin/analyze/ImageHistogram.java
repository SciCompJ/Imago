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
import net.sci.array.Histograms;
import net.sci.array.color.RGB16Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt8Array;
import net.sci.image.Image;

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
		Histogram histo = new Histogram(image);
		
        String name = "Histogram of " + image.getName();
		ImageHistogramDisplayFrame histoFrame = new ImageHistogramDisplayFrame(frame, name, histo);
		
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
    class Histogram
    {
        /** the reference image, for retrieving meta-data and eventually recomputing the histogram */
        Image refImage;
        
        /**
         * The range of values of image. Computed once at initialization.
         */
        double[] imageValueRange;
        
        /** The position of histogram bins. */
        double[] xData;
        
        /**
         * The frequency count for each channel. On array per channel, the same
         * size as xData.
         */
        int[][] yData;
        
        double minValue = 0.0;
        double maxValue = 255.0;
        int nBins = 256;
        
        /** The name of each channel. Same size as yData. */
        String[] channelNames;

        public Histogram(Image image)
        {
            this.refImage = image;
            
            computeDefaultValueRange();
            update();
        }
        
        private void computeDefaultValueRange()
        {
            Array<?> array = this.refImage.getData();
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
        
        /**
         * Updates histogram data based on the inner bounds and bin number. Image
         * values will be iterated.
         */
        public void update()
        {
            Array<?> array = this.refImage.getData();
            
            if (array instanceof UInt8Array)            
            {
                computeHistogramData_UInt8((UInt8Array) array);
            }
            else if (array instanceof RGB8Array)            
            {
                computeHistogramData_RGB8((RGB8Array) array);
            }
            else if (array instanceof RGB16Array)            
            {
                computeHistogramData_RGB16((RGB16Array) array);
            }
            else if (array instanceof ScalarArray)            
            {
                computeHistogramData_Scalar((ScalarArray<?>) array);
            }
            else
            {
                throw new RuntimeException("Unable to compute histogram for array class: " + array.getClass());
            }
        }
        
        private void computeHistogramData_UInt8(UInt8Array array)
        {
            this.xData = new double[256];
            for (int i = 0; i < 256; i++)
            {
                xData[i] = i;
            }
            
            // allocate memory for result
            this.yData = new int[1][];
            this.yData[0] = Histograms.histogramUInt8(array);
            
            this.channelNames = new String[] {"Intensity"};
        }
        
        private void computeHistogramData_Scalar(ScalarArray<?> array)
        {
            // compute the sizeX of an individual bin
            double binWidth = (maxValue - minValue) / (nBins - 1);
            
            // allocate memory for result
            double[] range = new double[] {minValue, maxValue};

            this.xData = new double[nBins];
            for (int i = 0; i < nBins; i++)
            {
                xData[i] = range[0] + i * binWidth;
            }
            
            // allocate memory for result
            this.yData = new int[1][];
            this.yData[0] = Histograms.histogramScalar(array, range, nBins);
            
            this.channelNames = new String[] {"Intensity"};
        }
        
        private void computeHistogramData_RGB8(RGB8Array array)
        {
            this.xData = new double[256];
            for (int i = 0; i < 256; i++)
            {
                xData[i] = i;
            }
            this.yData = Histograms.histogramRGB8((RGB8Array) array);
            
            this.channelNames = new String[] {"Red", "Green", "Blue"};
        }

        private void computeHistogramData_RGB16(RGB16Array array)
        { 
            int[][] histo = Histograms.histogramRGB16((RGB16Array) array);
            
            this.xData = convertToDouble(histo[0]);
            this.yData = new int[3][];
            for (int i = 0; i < 3; i++)
            {
                this.yData[i] = histo[i+1];
            }
            this.channelNames = new String[] {"Red", "Green", "Blue"};
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

    /**
     * The frame that displays the histogram.
     */
    class ImageHistogramDisplayFrame extends ImagoFrame
    {
        Histogram histogram;
        
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
        
        public ImageHistogramDisplayFrame(ImagoFrame parentFrame, String name, Histogram histogram) 
        {
            super(parentFrame, name);
            
            this.histogram = histogram;
            
            createChart();
            
            setupWidgets();
            setupLayout();
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

            if (this.histogram.canUpdateBounds())
            {
                this.histogram.computeDefaultValueRange();
                
                this.minValueSpinner = new JSpinner(new SpinnerNumberModel(this.histogram.minValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
                getTextField(this.minValueSpinner).setColumns(8);
                this.minValueSpinner.addChangeListener(evt -> {
                    this.histogram.minValue = ((SpinnerNumberModel) minValueSpinner.getModel()).getNumber().doubleValue();
                    this.histogram.update();
                    updateDisplay();
                });
                this.maxValueSpinner = new JSpinner(new SpinnerNumberModel(this.histogram.maxValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
                getTextField(this.maxValueSpinner).setColumns(8);
                this.maxValueSpinner.addChangeListener(evt -> {
                    this.histogram.maxValue = ((SpinnerNumberModel) maxValueSpinner.getModel()).getNumber().doubleValue();
                    this.histogram.update();
                    updateDisplay();
                });
                this.nBinsSpinner = new JSpinner(new SpinnerNumberModel(this.histogram.nBins, 10, 1024, 1));
                this.nBinsSpinner.addChangeListener(evt -> {
                    this.histogram.nBins = ((SpinnerNumberModel) nBinsSpinner.getModel()).getNumber().intValue();
                    this.histogram.update();
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
            
            if (this.histogram.canUpdateBounds())
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
            int nChannels = this.histogram.yData.length;

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
                double[] data = convertToDouble(this.histogram.yData[c]);
                series[c] = chart.addSeries(this.histogram.channelNames[c], this.histogram.xData, data);
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
        
        public void updateDisplay()
        {
            Map<String, XYSeries> map = chart.getSeriesMap();
            for (int c = 0; c < this.histogram.yData.length; c++)
            {
                String name = this.histogram.channelNames[c];
                XYSeries series = map.get(name);
                
                // check log scale
                double[] data = convertToDouble(this.histogram.yData[c]);
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
                    data[0] = 0;
                    data[data.length-1] = 0;
                }
                
                series.replaceData(this.histogram.xData, data, null);
            }
            
            this.chartPanel.repaint();
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
