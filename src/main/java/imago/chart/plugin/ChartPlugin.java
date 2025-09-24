/**
 * 
 */
package imago.chart.plugin;

import imago.gui.ImagoFrame;
import imago.chart.ChartFrame;
import imago.gui.FramePlugin;

/**
 * Specialization of the Plugin interface for Chartframe plugins.
 * 
 * Simply consists in providing a default implementation for the isEnabled
 * method, that returns true if the parent frame is an instance of ChartFrame
 * (and hence may contain a chart).
 * 
 * @author dlegland
 *
 */
public interface ChartPlugin extends FramePlugin
{
    /**
     * Defines whether this plugin should be enabled for the given frame.
     * 
     * Returns true if the calling frame is an instance of ImageFrame and it
     * contains a valid image.
     * 
     * @param frame
     *            the calling frame
     * @return true if the calling frame contains an image.
     */
    public default boolean isEnabled(ImagoFrame frame)
    {
        if (!(frame instanceof ChartFrame))
        {
            return false;
        }
        
        return true;
    }
}
