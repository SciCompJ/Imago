/**
 * 
 */
package imago.image.plugins;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Image;

/**
 * Specialization of the {@code FramePlugin} interface, that expect the parent
 * frame to be an instance of {@code ImageFrame}.
 * 
 * Simply consists in providing a default implementation for the
 * {@code isEnabled} method, that returns true if the parent frame contains an
 * image.
 * 
 * 
 * @see ImageFrame
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
        if (frame instanceof ImageFrame imageFrame)
        {
            Image image = imageFrame.getImageHandle().getImage();
            return image != null;
        }

        return false;
    }
}
