/**
 * 
 */
package imago.plugin.image.binary;

import java.util.Locale;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.morphology.BinaryMorphologicalFilter;
import net.sci.image.morphology.Strel;
import net.sci.image.morphology.filter.BinaryClosing;
import net.sci.image.morphology.filter.BinaryDilation;
import net.sci.image.morphology.filter.BinaryErosion;
import net.sci.image.morphology.filter.BinaryGradient;
import net.sci.image.morphology.filter.BinaryOpening;
import net.sci.image.morphology.strel.Strel2D;
import net.sci.image.morphology.strel.Strel3D;


/**
 * Computes morphological filtering on the current binary image.
 *
 * @see imago.plugin.image.process.ImageMorphologicalFilter
 * 
 * @author David Legland
 */
public class BinaryImageMorphologicalFilter implements FramePlugin
{
    // =============================================================
    // Class variables

//    /**
//     * The operation to apply to the image.
//     */
//    Operation op = Operation.DILATION;

    /**
     * The shape of the structuring element for 2D images.
     */
    Strel2D.Shape shape2d = Strel2D.Shape.DISK;

    /**
     * The shape of the structuring element for 3D images.
     */
    Strel3D.Shape shape3d = Strel3D.Shape.BALL;

    /**
     * The radius of the structuring element, in pixels.
     */
    int radius = 2;
  
    private String[] operationStrings = new String[] {"Dilation", "Erosion", "Opening", "Closing", "Gradient"};
    private String[] operationSuffixes = new String[] {"Dil", "Ero", "Op", "Cl", "Gr"};
    
	public BinaryImageMorphologicalFilter() 
	{
	}
	
	@Override
	public void run(ImagoFrame frame, String args) 
	{
        System.out.println("binary morphological filter");

        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();
        if (nd != 2 && nd != 3)
        {
            System.err.println("Requires image of dimensionality equal to 2 or 3");
            return;
        }
        
        // Create dialog for entering parameters
        GenericDialog gd = new GenericDialog(frame, "Binary Morphological Filter");
        gd.addChoice("Operation", operationStrings, operationStrings[1]);
//        gd.addChoice("Operation", Operation.getAllLabels(), 
//                this.op.toString());
        if (nd == 2)
        {
            gd.addChoice("Element", Strel2D.Shape.getAllLabels(), 
                    this.shape2d.toString());
            gd.addNumericField("Radius (in pixels)", this.radius, 0);
        }
        else
        {
            gd.addChoice("Element", Strel3D.Shape.getAllLabels(), 
                    this.shape3d.toString());
            gd.addNumericField("Radius (in voxels)", this.radius, 0);
        }
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        // extract chosen parameters
        int opIndex = gd.getNextChoiceIndex();
        Strel strel;
        String shapeSuffix;
        if (nd == 2)
        {
            this.shape2d = Strel2D.Shape.fromLabel(gd.getNextChoice());
            shapeSuffix = this.shape2d.suffix();
            this.radius  = (int) gd.getNextNumber();
            strel = shape2d.fromRadius(radius);
        }
        else
        {
            this.shape3d = Strel3D.Shape.fromLabel(gd.getNextChoice());
            shapeSuffix = this.shape3d.suffix();
            this.radius  = (int) gd.getNextNumber();
            strel = shape3d.fromRadius(radius);
        }
        
        // Create the morphological filter
        BinaryMorphologicalFilter algo;
        switch (opIndex)
        {
            case 0: algo = new BinaryDilation(strel); break;
            case 1: algo = new BinaryErosion(strel); break;
            case 2: algo = new BinaryOpening(strel); break;
            case 3: algo = new BinaryClosing(strel); break;
            case 4: algo = new BinaryGradient(strel); break;
            default:
                throw new RuntimeException("Unknown Operation index");
        }
        
        // Execute core of the plugin on the array of original image
        Image resultImage = imageFrame.runOperator(algo, image);
        
        // setup name of result image
        String suffix = String.format(Locale.ENGLISH, "%s%s%02d", operationSuffixes[opIndex], shapeSuffix, radius);
        resultImage.setName(image.getName() + "-" + suffix);
        
		frame.getGui().createImageFrame(resultImage); 
	}

    /**
     * Returns true if the current frame contains a binary image.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @return true if the frame contains a binary image
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.isBinaryImage();
    }
}
