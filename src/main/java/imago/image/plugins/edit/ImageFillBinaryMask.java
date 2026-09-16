/**
 * 
 */
package imago.image.plugins.edit;

import java.util.Collection;
import java.util.prefs.Preferences;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.plugins.ImageFramePlugin;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.image.Image;

/**
 * 
 */
public class ImageFillBinaryMask implements ImageFramePlugin
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
        // get current image data
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        Image image = handle.getImage();
        Array<?> array = image.getData();

        if (!(array instanceof ScalarArray2D))
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a ScalarArray2D", "Image Type error");
            return;
        }

        ImagoGui gui = frame.getGui();
        ImagoApp app = gui.getAppli();
        Collection<String> binaryImageNames = ImageHandle.getAll(app).stream()
                .filter(h -> h.getImage().isBinaryImage())
                .map(h -> h.getName())
                .toList();
        if (binaryImageNames.isEmpty())
        {
            ImagoGui.showErrorDialog(frame, "Requires at least one binary image open", "Plugin error");
            return;
        }

        String[] imageNameArray = binaryImageNames.toArray(new String[]{});
        String firstImageName = imageNameArray[0];

        Preferences prefs = Preferences.userRoot().node("imago/image/draw");
        double defaultValue = prefs.getDouble("DrawValue", 0);
        boolean defaultFillNaN = prefs.getBoolean("FillNaN", false);
        
        // Creates the dialog
        GenericDialog gd = new GenericDialog(frame, "Fill Binary Mask");
        gd.addChoice("Mask Image: ", imageNameArray, firstImageName);
        gd.addNumericField("Value", defaultValue, 2);
        gd.addCheckBox("Fill with NaN values", defaultFillNaN);
        gd.addCheckBox("Create Result Image", true);
        gd.showDialog();
        
        if (gd.wasCanceled()) 
        {
            return;
        }
        
        // parse dialog results
        Image maskImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        double value = gd.getNextNumber();
        boolean fillWithNaN  = gd.getNextBoolean();
        boolean createResult = gd.getNextBoolean();
        
        prefs.putDouble("DrawValue", value);
        prefs.putBoolean("FillNaN", fillWithNaN);
        
        if (fillWithNaN) value = Double.NaN;
        
        // extract mask array and check validity
        System.out.println("mask image: " + maskImage.getName());
        Array<?> mask = maskImage.getData();
        if (mask.elementClass() != Binary.class)
        {
            ImagoGui.showErrorDialog(frame, "Mask image must be binary", "Image Type Error");
            return;
        }
        if (!Arrays.isSameSize(array, mask))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimensions", "Image Size Error");
            return;
        }
        
        BinaryArray binaryMask = BinaryArray.wrap(mask);
        
        // create result array (may be the original array)
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ScalarArray<?> scalarArray = ScalarArray.wrap((Array<? extends Scalar>) array);
        ScalarArray<?> res = createResult ? scalarArray.duplicate() : scalarArray;
        
        // process
        for (int[] pos : res.positions())
        {
            if (binaryMask.getBoolean(pos))
            {
                res.setValue(pos, value);
            }
        }
       
        // create or update result image
        if (createResult)
        {
            Image resultImage = new Image(res, image);
            resultImage.setName(image.getName() + "-masked");

            // add the image document to GUI
            ImageFrame.create(resultImage, frame);
        }
        else
        {
            // notify changes
            handle.notifyImageHandleChange(ImageHandle.Event.IMAGE_MASK | ImageHandle.Event.CHANGE_MASK);
        }
    }

}
