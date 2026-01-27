/**
 * 
 */
package imago.shape.plugins.edit;

import imago.gui.Dialogs;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import net.sci.geom.Geometry;

/**
 * Sets the selected geometry as selection of an image.
 */
public class SetAsImageSelection implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        Geometry geom = sm.getSelectedHandle().getGeometry();
        ImageHandle imageHandle = Dialogs.chooseImage(frame, "Set Image Selection", "Image");
        if (imageHandle == null) 
        {
            return;
        }
        
        for (ImageFrame imageFrame : ImageFrame.getImageFrames(frame.getGui()))
        {
            if (imageFrame.getImageHandle() == imageHandle)
            {
                imageFrame.getImageViewer().setSelection(geom);
                imageFrame.repaint();
            }
        }
        
        imageHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
    }
}
