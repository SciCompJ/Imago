/**
 * 
 */
package imago.image.plugin.shape;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.shape.Padding;
import net.sci.image.Image;

/**
 * Add constant values around borders of image.
 * 
 * The type of the value to add is determined by image type. 
 */
public class ImageAddBorders implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        if (!(array instanceof ScalarArray))
        {
            throw new RuntimeException("Requires a scalar array as input");
        }
        int nd = array.dimensionality();
        
        // create a dialog for the user to choose options
        GenericDialog gd = new GenericDialog(frame, "Extend Borders");
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Padding dim. " + (d+1), 10, 0);
        }
        gd.addNumericField("Padding Value", 0.0, 2);
        gd.addCheckBox("Create View", false);
        
        // wait the user to choose
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        int[] padSizes = new int[nd];
        for (int d = 0; d < nd; d++)
        {
            padSizes[d] = (int) gd.getNextNumber();
        }
        double padValue = gd.getNextNumber();
        boolean createView = gd.getNextBoolean();
        
        ScalarArray<?> paddedArray = Padding.padScalar((ScalarArray<?>) array, padSizes, padSizes, padValue);
        if (!createView)
        {
            paddedArray = paddedArray.duplicate();
        }
        
        Image resultImage = new Image(paddedArray, image);
        resultImage.setName(image.getName() + "-addBorders");
        
        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }
    
    
    public boolean isEnabled(ImagoFrame frame)
    {
        if (!(frame instanceof ImageFrame)) return false;
        Image image = ((ImageFrame) frame).getImageHandle().getImage();
        if (!(image.getData() instanceof ScalarArray)) return false;
        return true;
    } 
}
