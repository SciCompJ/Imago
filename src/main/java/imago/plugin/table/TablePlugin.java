/**
 * 
 */
package imago.plugin.table;

import imago.gui.ImagoFrame;
import imago.gui.TableFrame;
import imago.gui.FramePlugin;

/**
 * Specialization of the Plugin interface for Table plugins.
 * 
 * Simply consists in providing a default implementation for the isEnabled
 * method, that returns true if the parent frame contains a table.
 * 
 * @author dlegland
 *
 */
public interface TablePlugin extends FramePlugin
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
        if (!(frame instanceof TableFrame))
        {
            return false;
        }
        
        return true;
    }
}
