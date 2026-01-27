/**
 * 
 */
package imago.shape.plugins.edit;

import imago.app.shape.Shape;
import imago.gui.Dialogs;
import imago.gui.ImagoFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;

/**
 * Opens a dialog to choose an image, and copies the selected shapes in the
 * "shapes" field of the chosen image.
 */
public class CopyToImageShapes implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        ImageHandle imageHandle = Dialogs.chooseImage(frame, "Set Image Selection", "Image to Update");
        if (imageHandle == null) 
        {
            return;
        }
        
        for (GeometryHandle handle : sm.getSelectedHandles())
        {
            Shape shape = new Shape(handle.getGeometry());
            imageHandle.addShape(shape);
        }
        
        imageHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
    }
}
