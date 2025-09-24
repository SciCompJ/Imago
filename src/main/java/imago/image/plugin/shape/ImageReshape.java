/**
 * 
 */
package imago.image.plugin.shape;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.shape.Reshape;
import net.sci.image.Image;
import net.sci.util.MathUtils;

/**
 * Reshape an image by specifying new dimensions.
 * 
 * @author David Legland
 *
 */
public class ImageReshape implements FramePlugin
{
    public ImageReshape()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        Image image = ((ImageFrame) frame).getImageHandle().getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();

        // number of elements of initial array
        long numel = MathUtils.prod(array.size());

        // initialize results
        int[] newDims = array.size();
        boolean createView = false;

        while (true)
        {
            GenericDialog gd = new GenericDialog(frame, "Reshape");
            for (int d = 0; d < nd; d++)
            {
                gd.addNumericField("Size dim. " + (d + 1), newDims[d], 0);
            }
            gd.addCheckBox("Create View", createView);
            gd.showDialog();

            if (gd.getOutput() == GenericDialog.Output.CANCEL)
            {
                return;
            }

            // parse dialog results
            for (int d = 0; d < nd; d++)
            {
                newDims[d] = (int) gd.getNextNumber();
            }
            createView = gd.getNextBoolean();

            // If compatibility of dimensions is met, break loop
            if (MathUtils.prod(newDims) == numel)
            {
                break;
            }

            ImagoGui.showErrorDialog(frame, "Output element number should match input element number: " + numel);
        };

        // create reshape operator
        Reshape op = new Reshape(newDims);

        // apply operator on current image
        Array<?> result = createView ? op.view(array) : op.process(array);

        // create result image
        Image resultImage = new Image(result, image);
        resultImage.setName(image.getName() + "-reshape");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
}
