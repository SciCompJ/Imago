/**
 * 
 */
package imago.image.plugins.shape;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.process.MaxProjection;
import net.sci.array.numeric.process.MeanIntensityProjection;
import net.sci.array.numeric.process.MedianProjection;
import net.sci.array.numeric.process.MinProjection;
import net.sci.array.shape.Squeeze;
import net.sci.image.Image;

/**
 * @author dlegland
 *
 */
public class ImageOrthogonalProjection implements FramePlugin
{
    private static final String[] opNames = {"Max.", "Min.", "Mean", "Median"};

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        Image image = iFrame.getImageHandle().getImage();
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray))
        {
            frame.showErrorDialog("Requires a scalar image as input", "Data Type Error");
            return;
        }
        
        int nd = array.dimensionality();

        GenericDialog gd = new GenericDialog(frame, "Orthogonal Projection");
        gd.addNumericField("Dimension", 0, 0);
        gd.addChoice("Operation", opNames, opNames[0]);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        int dim = (int) gd.getNextNumber();
        int opIndex = gd.getNextChoiceIndex();
        if (dim >= nd)
        {
            frame.showErrorDialog("Can not project along a dimension greater than " + nd, "Dimension Error");
            return;
        }
        
        
        ScalarArray<?> result = switch (opIndex)
        {
            case 0 -> new MaxProjection(dim).processScalar((ScalarArray<?>) array);
            case 1 -> new MinProjection(dim).processScalar((ScalarArray<?>) array);
            case 2 -> new MeanIntensityProjection(dim).processScalar((ScalarArray<?>) array);
            case 3 -> new MedianProjection(dim).processScalar((ScalarArray<?>) array);
            default -> throw new RuntimeException("Unknown operation string");
        };
        
        result = (ScalarArray<?>) new Squeeze().process(result);
        
        Image resultImage = new Image(result, image);
        resultImage.setName(image.getName() + "-proj");
        
        ImageFrame.create(resultImage, frame);
    }
}
