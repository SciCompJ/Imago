/**
 * 
 */
package imago.image.plugins.edit;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.color.ColorMap;
import net.sci.image.Image;

/**
 * Choose the colormap of the current scalar image.
 * 
 * @deprecated replaced by ImageSetColorMapFactory
 * 
 * @author David Legland
 *
 */
@Deprecated
public class ImageSetColorMap implements FramePlugin
{
    ColorMap colorMap;

    public ImageSetColorMap(ColorMap colorMap)
    {
        this.colorMap = colorMap;
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
        // retrieve image handle
        ImageFrame viewer = (ImageFrame) frame;
        ImageHandle handle = viewer.getImageHandle();
        
        // update image
        Image image = handle.getImage();
        image.getDisplaySettings().setColorMap(this.colorMap);
        
        // notify changes
        handle.notifyImageHandleChange(ImageHandle.Event.LUT_MASK | ImageHandle.Event.CHANGE_MASK);
    }
}
