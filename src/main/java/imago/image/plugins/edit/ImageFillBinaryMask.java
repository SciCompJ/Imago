/**
 * 
 */
package imago.image.plugins.edit;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.Image;

/**
 * 
 */
public class ImageFillBinaryMask implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageFillBinaryMask()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ImagoGui gui = frame.getGui();
        ImagoApp app = gui.getAppli();
        Collection<String> imageNames = ImageHandle.getAllNames(app);

        // Case of no open document with image
        if (imageNames.size() == 0)
        {
            return;
        }
        
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = imageNameArray[0];

        // Creates the dialog
        GenericDialog gd = new GenericDialog(frame, "Fill Binary Mask");
        gd.addChoice("Intensity Image: ", imageNameArray, firstImageName);
        gd.addChoice("Mask Image: ", imageNameArray, firstImageName);
        gd.addNumericField("Value", 0, 2);
        gd.showDialog();
        
        if (gd.wasCanceled()) 
        {
            return;
        }
        
        // parse dialog results
        Image refImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        Image markerImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        double value = 0.0;
        try 
        {
            value = gd.getNextNumber();
        }
        catch (Exception ex)
        {
            value = Double.NaN;
        }
        
        // extract arrays and check dimensions
        Array<?> array = refImage.getData();
        Array<?> mask = markerImage.getData();
        if (!Arrays.isSameSize(array, mask))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimensions", "Image Size Error");
            return;
        }
        if (mask.elementClass() != Binary.class)
        {
            ImagoGui.showErrorDialog(frame, "Mask image must be binary", "Image Type Error");
            return;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ScalarArray<?> res = ScalarArray.wrap((Array<? extends Scalar>) array.duplicate());

        BinaryArray binaryMask = BinaryArray.wrap(mask);
        
        for (int[] pos : res.positions())
        {
            if (binaryMask.getBoolean(pos))
            {
                res.setValue(pos, value);
            }
        }
       
        Image resultImage = new Image(res, refImage);
        resultImage.setName(refImage.getName() + "-masked");
        
        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

}
