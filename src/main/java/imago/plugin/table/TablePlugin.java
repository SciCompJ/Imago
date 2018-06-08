/**
 * 
 */
package imago.plugin.table;

import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
import imago.gui.Plugin;

/**
 * Specialization of the Plugin interface for Table plugins.
 * 
 * Simply consists in providing a default implementation for the isEnabled
 * method, that returns true if the parent frame contains a table.
 * 
 * @author dlegland
 *
 */
public interface TablePlugin extends Plugin
{
    /**
     * Defines whether this plugin should be enabled for the given frame.
     * 
     * Returns true if the calling frame is an instance of ImagoDocViewer and it
     * contains a valid image.
     * 
     * @param frame
     *            the calling frame
     * @return true if the calling frame contains an image.
     */
    public default boolean isEnabled(ImagoFrame frame)
    {
        if (!(frame instanceof ImagoTableFrame))
        {
            return false;
        }
        
        return true;
    }
}
