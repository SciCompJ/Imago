/**
 * 
 */
package imago.image.plugins.vectorize;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.viewers.LabelMapViewer3D;
import net.sci.array.Array;
import net.sci.array.numeric.Int;
import net.sci.image.Image;

/**
 * Computes isosurface of each region within a 3D label map.
 * 
 * @author dlegland
 *
 * @see LabelMapViewer3D
 */
public class LabelMap3DIsosurfaces implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        ImageHandle handle = iFrame.getImageHandle();
        Image image = handle.getImage();
        
        // check input data type and dimensions
        Array<?> array = image.getData();
        if (!(array.elementInstanceOf(Int.class)))
        {
            frame.showErrorDialog("Requires an array of scalar data as input", "Data Type Error");
            return;
        }
        int nd = array.dimensionality();
        if (nd != 3)
        {
            frame.showErrorDialog("Requires a 3D image", "Dimensionality Error");
            return;
        }

        // delegates most of the processing to the static factory within the LabelMapViewer class
        LabelMapViewer3D.create(image, frame);
    }
    
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        if (frame instanceof ImageFrame imageFrame)
        {
            Image image = imageFrame.getImageHandle().getImage();
            if (image == null) return false;

            return image.isLabelImage() && image.getDimension() == 3;
        }
        
        return false;
    }
}
