/**
 * 
 */
package imago.image.plugin;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Image;

/**
 * Specialization of the Plugin interface for Image plugins.
 * 
 * Simply consists in providing a default implementation for the isEnabled
 * method, that returns true if the parent frame contains an image.
 * 
 * @author dlegland
 *
 */
public interface ImageFramePlugin extends FramePlugin
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
        if (!(frame instanceof ImageFrame))
        {
            return false;
        }
        
        Image image = ((ImageFrame) frame).getImageHandle().getImage();
        
        return image != null;
    }
}
