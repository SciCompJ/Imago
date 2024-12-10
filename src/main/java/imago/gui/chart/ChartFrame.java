/**
 * 
 */
package imago.gui.chart;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.internal.chartpart.Chart;

import imago.app.ImagoApp;
import imago.gui.GuiBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;

/**
 * An Imago Frame that displays a Chart.
 * 
 * @author dlegland
 *
 */
public class ChartFrame extends ImagoFrame
{
    // ===================================================================
    // Static methods
    
    /**
     * Creates a new frame for displaying a chart, located with respect to the
     * specified frame.
     * 
     * @param chart
     *            the chart to display in the frame
     * @param frameTitle
     *            the title of the frame
     * @param parentFrame
     *            (optional) an existing frame used to locate the new frame. If
     *            null, a new ImagoGui is created.
     * @return a new frame displaying the input chart
     */
    public static final ChartFrame create(Chart<?,?> chart, String frameTitle, ImagoFrame parentFrame)
    {
        ChartFrame frame = create(chart, parentFrame);
        frame.setTitle(frameTitle);
        return frame;
    }
    
    /**
     * Creates a new frame for displaying a chart, located with respect to the
     * specified frame.
     * 
     * @param chart
     *            the chart to display in the frame
     * @param parentFrame
     *            (optional) an existing frame used to locate the new frame. If
     *            null, a new ImagoGui is created.
     * @return a new frame displaying the input chart
     */
    public static final ChartFrame create(Chart<?,?> chart, ImagoFrame parentFrame)
    {
        // retrieve gui, or create one if necessary
        ImagoGui gui = parentFrame != null ? parentFrame.getGui() : new ImagoGui(new ImagoApp());

        // Create the frame
        ChartFrame frame = new ChartFrame(gui, chart);
        gui.updateFrameLocation(frame, parentFrame);

        // link the frames
        gui.addFrame(frame);
        if (parentFrame != null)
        {
            parentFrame.addChild(frame);
        }
        
        frame.setVisible(true);
        return frame;
    }
    
    
    // ===================================================================
    // Class variables

    /**
     * The chart.
     */
    Chart<?,?> chart;
    
    
    // ===================================================================
    // Constructor
    
    /**
     * @param parent 
     */
    public ChartFrame(ImagoGui gui, Chart<?,?> chart)
    {
        super(gui, "Chart Frame");
        this.chart = chart;

        // create menu
        GuiBuilder builder = new GuiBuilder(this);
        builder.createMenuBar();
        
        this.setTitle("Table Example");
        this.jFrame.pack();
        this.setVisible(true);
        
        jFrame.doLayout();
        updateTitle();
        
        // setup window listener
        this.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.jFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                gui.removeFrame(ChartFrame.this);
                ChartFrame.this.close();
            }           
        });
        
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        try
        {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    // add a panel containing the chart 
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    XChartPanel chartPanel = new XChartPanel(chart);
                    jFrame.add(chartPanel);
                    
                    // Display the window.
                    jFrame.pack();
                    jFrame.setVisible(true);
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
        
        putFrameMiddleScreen();
    }
    
    private void putFrameMiddleScreen()
    {
        // set up frame size depending on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(800, screenSize.width - 100);
        int height = Math.min(700, screenSize.width - 100);
        Dimension frameSize = new Dimension(width, height);
        this.jFrame.setSize(frameSize);

        // set up frame position depending on frame size
        int posX = (screenSize.width - width) / 4;
        int posY = (screenSize.height - height) / 4;
        this.jFrame.setLocation(posX, posY);
    }

    // ===================================================================
    // General methods
    
    public void updateTitle()
    {
        String titleString = "Chart Frame";
        this.setTitle(titleString);
    }
    
    public Chart<?,?> getChart()
    {
        return this.chart;
    }
}
