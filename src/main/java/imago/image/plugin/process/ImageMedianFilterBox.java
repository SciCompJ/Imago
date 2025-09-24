/**
 * 
 */
package imago.image.plugin.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.process.ScalarArrayOperator;
import net.sci.image.Image;
import net.sci.image.filtering.MedianFilterBox;
import net.sci.image.filtering.MedianFilterBoxSliding;

/**
 * Applies median filtering within box on a multidimensional image.
 * 
 * @author David Legland
 */
public class ImageMedianFilterBox implements FramePlugin
{
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();

        GenericDialog gd = new GenericDialog(frame, "Median Filter");
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Size dim. " + (d + 1), 3, 0);
        }
        gd.showDialog();

        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        { return; }

        // parse dialog results
        int[] diameters = new int[nd];
        for (int d = 0; d < nd; d++)
        {
            diameters[d] = (int) gd.getNextNumber();
        }

        // create median box operator
        ScalarArrayOperator filter;
        if (image.getData() instanceof UInt8Array && (nd == 2 || nd == 3))
        {
            // use sliding operator, faster than naive one
            filter = new MedianFilterBoxSliding(diameters);
        }
        else
        {
            // use naive operator, working with any scalar type
            filter = new MedianFilterBox(diameters);
        }

        // apply operator on current image
        Image result = ((ImageFrame) frame).runOperator(filter, image);
        result.setName(image.getName() + "-medFilt");

        // add the image document to GUI
        ImageFrame.create(result, frame);
	}

    /**
     * Returns true if the current frame contains a scalar image or a vector
     * image.
     * 
     * @param frame
     *            the frame containing reference to this plugin
     * @return true if the frame contains a scalar or vector image.
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

        return image.isScalarImage() || image.isVectorImage();
    }
}
