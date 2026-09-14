/**
 * 
 */
package imago.image.plugins.process;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.Image;
import net.sci.image.segmentation.OtsuThreshold;

/**
 * Applies threshold to an image by computing threshold value within the
 * specified binary mask using Otsu algorithm.
 * 
 * Principle of Otsu method is to identify threshold value that maximizes the
 * variance between the classes, or equivalently to minimize the sum of
 * variances within each class.
 * 
 * @see ImageOtsuThreshold
 */
public class ImageOtsuThresholdWithMask implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageOtsuThresholdWithMask()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String args)
    {
        ImagoGui gui = frame.getGui();
        ImagoApp app = gui.getAppli();
        Collection<String> imageNames = ImageHandle.getAllNames(app);

        // if there is no image, escape.
        if (imageNames.size() == 0)
        {
            return;
        }
        
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = imageNameArray[0];
                
        // Creates the dialog
        GenericDialog gd = new GenericDialog(frame, "Ostu Threshold With Mask");
        gd.addChoice("Intensity Image: ", imageNameArray, firstImageName);
        gd.addChoice("Binary Mask: ", imageNameArray, firstImageName);
        gd.addCheckBox("Set Mask Outside as true", false);
        
        gd.showDialog();
        
        if (gd.wasCanceled()) 
        {
            return;
        }
        
        // parse dialog results
        Image inputImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        Image maskImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        boolean bgValue = gd.getNextBoolean();

        // check data type of input images
        if (!inputImage.getData().elementInstanceOf(Scalar.class))
        {
            ImagoGui.showErrorDialog(frame, "Intensity image must contain scalar data", "Image Data Type Error");
            return;
        }
        if (!maskImage.getData().elementInstanceOf(Binary.class))
        {
            ImagoGui.showErrorDialog(frame, "Intensity image must contain scalar data", "Image Data Type Error");
            return;
        }

        // extract arrays and check dimensions
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ScalarArray array = ScalarArray.wrap((Array<Scalar>) inputImage.getData());
        BinaryArray mask = BinaryArray.wrap(maskImage.getData());
        if (!Arrays.isSameSize(array, mask))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimensions", "Dimension Error");
            return;
        }
        
        OtsuThreshold op = new OtsuThreshold();
        double thresh = op.computeThresholdValue(array, mask);

        // compute result
        long t0 = System.nanoTime();
        BinaryArray res = BinaryArray.create(array.size());
        res.fillBooleans(pos -> mask.getBoolean(pos) ? array.getValue(pos) >= thresh : bgValue);
        long t1 = System.nanoTime();

        // display elapsed time
        if (frame instanceof ImageFrame)
        {
            double dt = (t1 - t0) / 1_000_000.0;
            ((ImageFrame) frame).showElapsedTime("Otsu Threshold", dt, inputImage);
        }

        Image resultImage = new Image(res, inputImage);
        resultImage.setName(inputImage.getName() + "-segOtsu");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

}
