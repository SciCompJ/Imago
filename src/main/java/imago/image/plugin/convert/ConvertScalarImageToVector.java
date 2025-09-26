/**
 * 
 */
package imago.image.plugin.convert;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.Float64VectorArray;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.array.numeric.VectorArray;
import net.sci.array.numeric.VectorArray2D;
import net.sci.image.Image;


/**
 * Converts a 3D image containing scalar data into a 2D image containing vector
 * data, by converting the third dimension into channels.
 * 
 * @see ConvertVectorImageToScalar
 */
public class ConvertScalarImageToVector implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ConvertScalarImageToVector()
    {
        super();
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
        if (!(array instanceof ScalarArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing scalar data", "Data Type Error");
            return;
        }
        
        Image result;
        if (array.dimensionality() == 3)
        {
            VectorArray<?, ?> vectArray = VectorArray2D.fromStack((ScalarArray3D<?>) array);
            result = new Image(vectArray, image);
        }
        else
        {
            // dimensions of input array
            int nd = array.dimensionality();
            int[] dims = array.size();
            int nChannels = dims[nd - 1];

            // create result array
            int[] dims2 = new int[nd - 1];
            System.arraycopy(dims, 0, dims2, 0, nd - 1);
            Float64VectorArray res = Float64VectorArray.create(dims2, nChannels);
            
            // iterate over positions of result array
            int[] pos = new int[nd];
            for (int[] pos2 : res.positions())
            {
                System.arraycopy(pos2, 0, pos, 0, nd-1);
                
                for (int c = 0; c < nChannels; c++)
                {
                    pos[nd-1] = c;
                    res.setValue(pos2, c, ((ScalarArray<?>) array).getValue(pos));
                }
            }

            // create the image corresponding to channels concatenation
            result = new Image(res, image);
        }

        result.setName(image.getName() + "-vector");

        // add the image document to GUI
        ImageFrame.create(result, frame);
    }
}
