/**
 * 
 */
package imago.gui.frames;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.internal.chartpart.Chart;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;

/**
 *  
 *
 */
public class ImagoChartFrame extends ImagoFrame 
{
	// ===================================================================
	// Class variables

    public static final ImagoChartFrame displayChart(ImagoFrame parentFrame, String title,
            @SuppressWarnings("rawtypes") Chart chart)
    {
        // Create and set up the window.
        final ImagoChartFrame frame = new ImagoChartFrame(parentFrame, title);
        JFrame jFrame = frame.getWidget();
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        // relocate with respect to parent frame
        Point pos0 = parentFrame.getWidget().getLocation();
        jFrame.setLocation(pos0.x + 30, pos0.y + 20);
        
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
        
        return frame;
    }

	// ===================================================================
	// Constructors
	
	public ImagoChartFrame(ImagoGui gui) 
	{
		super(gui);
	}
	
	public ImagoChartFrame(ImagoGui gui, String name) 
	{
		super(gui, name);
	}
	
	public ImagoChartFrame(ImagoFrame parent, String name) 
	{
		super(parent, name);
	}
	
	// ===================================================================
	// General methods
	
	
	// ===================================================================
	// Display management methods
	

}
