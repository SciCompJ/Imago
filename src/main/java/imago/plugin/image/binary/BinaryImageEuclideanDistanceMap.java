/**
 * 
 */
package imago.plugin.image.binary;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.image.Image;
import net.sci.image.binary.distmap.SaitoToriwakiDistanceTransform;

/**
 * Distance map to nearest background pixel/voxel, using Euclidean distance
 * transform.
 * 
 * @author David Legland
 *
 */
public class BinaryImageEuclideanDistanceMap implements FramePlugin
{
    /*
     * (non-Javadoc)
     * 
     * @see imago.gui.Plugin#run(ImagoFrame, String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // retrieve current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        // check type of input
        if (!(array instanceof BinaryArray))
        {
            frame.showErrorDialog("Requires a binary image input", "Data Type Error");
            return;
        }

        // also check dimensionality
        int nd = array.dimensionality();
        if (nd != 2 && nd != 3)
        {
            frame.showErrorDialog("Can process only 2D and 3D images", "Dimensionality Error");
            return;
        }

        // parse dialog results
        SaitoToriwakiDistanceTransform op = new SaitoToriwakiDistanceTransform();

        // Compute distance map
        Image resultImage = imageFrame.runImageOperator("Distance Transform", op, image);
        resultImage.setName(image.getName() + "-dist");

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
