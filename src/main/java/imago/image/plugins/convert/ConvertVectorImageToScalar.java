/**
 * 
 */
package imago.image.plugins.convert;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.Float64Array;
import net.sci.array.numeric.Vector;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;

/**
 * Converts a vector image (i.e. an image whose data are stored in an array of
 * {@code Vector} data into a scalar image with one dimension more than the
 * original image, by converting the channel dimension into a spatial dimension.
 * 
 * @see ConvertScalarImageToVector
 * @see CreateVectorImageRGB8View
 */
public class ConvertVectorImageToScalar implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ConvertVectorImageToScalar()
    {
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
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();

        if (image == null)
        {
            return;
        }
        Array<?> array = image.getData();
        if (array == null)
        {
            return;
        }
        if (!(Vector.class.isAssignableFrom(array.elementClass())))
        {
            ImagoGui.showErrorDialog(frame, "Requires a Vector image", "Data Type Error");
            return;
        }

        // wrap into a vector array
        @SuppressWarnings({ "unchecked", "rawtypes" })
        VectorArray<?,?> vectorArray = (VectorArray<?,?>) VectorArray.wrap((Array<Vector>) array);

        // dimensions of input array
        int nd = array.dimensionality();
        int[] dims = vectorArray.size();
        int nChannels = vectorArray.channelCount();

        // create result array
        int[] dims2 = new int[nd + 1];
        System.arraycopy(dims, 0, dims2, 0, nd);
        dims2[nd] = nChannels;
        Float64Array res = Float64Array.create(dims2);

        // iterate over positions of input array
        int[] pos2 = new int[nd + 1];
        for (int[] pos : vectorArray.positions())
        {
            System.arraycopy(pos, 0, pos2, 0, nd);
            double[] values = vectorArray.getValues(pos);

            for (int c = 0; c < nChannels; c++)
            {
                pos2[nd] = c;
                res.setValue(pos2, values[c]);
            }
        }

        // create the image corresponding to channels concatenation
        Image resultImage = new Image(res);
        resultImage.setName(image.getName() + "-scalar");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
}
