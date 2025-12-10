/**
 * 
 */
package imago.image.plugins.binary;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.ArrayOperator;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.image.Image;
import net.sci.image.morphology.reconstruction.BinaryFillHoles2D;
import net.sci.image.morphology.reconstruction.BinaryFillHoles3D;


/**
 * Fill holes within a binary image.
 * 
 * @author David Legland
 *
 */
public class BinaryImageFillHoles implements FramePlugin
{
    public BinaryImageFillHoles()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();

        Array<?> array = image.getData();
        if (!(array instanceof BinaryArray))
        {
            return;
        }

        ArrayOperator algo;
        if (array instanceof BinaryArray2D)
        {
            algo = new BinaryFillHoles2D();
        }
        else if (array instanceof BinaryArray3D)
        {
            algo = new BinaryFillHoles3D();
        }
        else
        {
            ImagoGui.showErrorDialog(imageFrame,
                    "Can not manage arrays with class: " + array.getClass().getName());
            return;
        }

        Image resultImage = imageFrame.runOperator(algo, image);
        resultImage.setName(image.getName() + "-FillHoles");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

    /**
     * Returns true if the current frame contains a binary image.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @return true if the frame contains a binary image
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.isBinaryImage();
    }
}
