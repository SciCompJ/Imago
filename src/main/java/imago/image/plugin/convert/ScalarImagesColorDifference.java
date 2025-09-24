/**
 * 
 */
package imago.image.plugin.convert;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import net.sci.array.color.RGB8Array;
import net.sci.array.color.ScalarArraysDifferenceView;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.Image;

/**
 * Plugin for representing the difference of intensities between two scalar
 * images as a composition of a magenta and green RGB image.
 * 
 * @author dlegland
 *
 */
public class ScalarImagesColorDifference implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        String[] imageNames = ImageHandle.getAllNames(frame.getGui().getAppli()).toArray(new String[]{});
        int index1 = 0;
        if (frame instanceof ImageFrame)
        {
            String imageName = ((ImageFrame) frame).getImageHandle().getName();
            index1 = findStringIndex(imageName, imageNames);
        }
        int index2 = (int) java.lang.Math.min(index1 + 1, imageNames.length-1);

        GenericDialog gd = new GenericDialog(frame, "Binary Math Operator");
        gd.addChoice("Image 1", imageNames, imageNames[index1]);
        gd.addNumericField("Min Value", 0, 0, "Intensity value associated to black");
        gd.addNumericField("Max Value", 255, 0, "Intensity value associated to magenta");
        gd.addChoice("Image 2", imageNames, imageNames[index2]);
        gd.addNumericField("Min Value", 0, 0, "Intensity value associated to black");
        gd.addNumericField("Max Value", 255, 0, "Intensity value associated to green");
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        String image1Name = gd.getNextChoice();
        double minValue1 = gd.getNextNumber();
        double maxValue1 = gd.getNextNumber();
        String image2Name = gd.getNextChoice();
        double minValue2 = gd.getNextNumber();
        double maxValue2 = gd.getNextNumber();
        
        // retrieve images from names
        Image image1 = ImageHandle.findFromName(frame.getGui().getAppli(), image1Name).getImage();
        if (!image1.isScalarImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
        }

        Image image2 = ImageHandle.findFromName(frame.getGui().getAppli(), image2Name).getImage();
        if (!image2.isScalarImage())
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a scalar array");
            return;
        }

        // extract arrays
        ScalarArray<?> array1 = (ScalarArray<?>) image1.getData();
        ScalarArray<?> array2 = (ScalarArray<?>) image2.getData();
        
        RGB8Array res = new ScalarArraysDifferenceView(array1, minValue1, maxValue1, array2, minValue2, maxValue2);
        res = res.duplicate();
        
        Image resultImage = new Image(res, image1);
        resultImage.setName(image1.getName() + "-diff");
        
        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

    private int findStringIndex(String string, String[] array)
    {
        if (string == null)
        {
            return 0;
        }

        for (int i = 0; i < array.length; i++)
        {
            if (string.equals(array[i]))
            {
                return i;
            }
        }
        
        return 0;
    }
}
