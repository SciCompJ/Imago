/**
 * 
 */
package imago.image.plugin.process;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.IntArray2D;
import net.sci.image.Image;
import net.sci.image.label.skeleton.ImageJSkeleton;


/**
 * Compute the skeleton of the current image, that must be either binary or a
 * label map.
 * 
 * Uses an adaptation of ImageJ's algorithm.
 * 
 * @see imago.image.plugin.binary.BinaryImageSkeleton
 * 
 * @author David Legland
 *
 */
public class LabelMapSkeleton implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public LabelMapSkeleton()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();

        Array<?> array = image.getData();
        if (!(array instanceof IntArray2D))
        {
            return;
        }

        ImageJSkeleton skel = new ImageJSkeleton();
        IntArray2D<?> res = skel.process2d((IntArray2D<?>) array);

        // create result image
        Image resultImage = new Image(res, image);
        resultImage.setName(image.getName() + "-skel");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

    /**
     * Returns true if the current frame contains a binary or label map image.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @return true if the frame contains a binary or label map image
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame)) return false;

        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null) return false;

        return image.isLabelImage();
    }
}
