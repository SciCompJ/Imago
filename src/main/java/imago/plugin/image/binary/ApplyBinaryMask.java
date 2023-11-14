/**
 * 
 */
package imago.plugin.image.binary;

import java.util.Collection;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.BinaryArray;
import net.sci.array.process.binary.BinaryMask;
import net.sci.image.Image;

/**
 * Computes a new image the same size and the same type as an input image, by
 * retaining values specified by a binary mask.
 * 
 * @author dlegland
 */
public class ApplyBinaryMask implements FramePlugin
{
    public ApplyBinaryMask()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        ImagoApp app = frame.getGui().getAppli();
        Collection<String> imageNames = ImageHandle.getAllNames(app);

        // Case of no open document with image
        if (imageNames.size() == 0)
        { return; }

        // retrieve necessary information
        String[] imageNameArray = imageNames.toArray(new String[] {});
        String firstImageName = imageNameArray[0];
        String secondImageName = imageNameArray[Math.min(1, imageNameArray.length - 1)];

        // Creates the dialog
        GenericDialog gd = new GenericDialog(frame, "Binary Mask");
        gd.addChoice("Reference Image: ", imageNameArray, firstImageName);
        gd.addChoice("Binary Image: ", imageNameArray, secondImageName);
        gd.showDialog();

        if (gd.wasCanceled())
        { return; }

        // parse dialog results
        Image baseImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        Image maskImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();

        // retrieve arrays
        Array<?> array = baseImage.getData();
        Array<?> mask = maskImage.getData();

        // check input validity
        if (!Arrays.isSameDimensionality(array, mask))
        {
            frame.showErrorDialog("Both images must have same dimensionality", "Dimensionality Error");
            return;
        }
        if (!Arrays.isSameSize(array, mask))
        {
            frame.showErrorDialog("Both images must have same size", "Image Size Error");
            return;
        }
        if (!(mask instanceof BinaryArray))
        {
            frame.showErrorDialog("Mask image must be binary", "Image Type Error");
            return;
        }

        // Create operator
        BinaryMask op = new BinaryMask();
        op.addAlgoListener(frame);

        // run operator
        long t0 = System.nanoTime();
        Array<?> result = op.process(array, BinaryArray.wrap(mask));
        long t1 = System.nanoTime();

        // show elapsed time
        double dt = (t1 - t0) / 1_000_000.0;
        ((ImageFrame) frame).showElapsedTime("Binary Mask", dt, baseImage);

        // Create result image
        Image resultImage = new Image(result, baseImage);
        resultImage.setName(baseImage.getName() + "-mask");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
}
