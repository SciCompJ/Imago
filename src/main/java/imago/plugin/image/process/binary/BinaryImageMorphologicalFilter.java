/**
 * 
 */
package imago.plugin.image.process.binary;

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
  
    private String[] operationStrings = new String[] {"Dilation", "Erosion", "Opening", "Closing"};
    
	public BinaryImageMorphologicalFilter() 
	{
	}
	
	@Override
	public void run(ImagoFrame frame, String args) 
	{
        System.out.println("binary morphological filter (2d)");

        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();
        if (nd != 2 && nd != 3)
        {
            System.err.println("Requires image of dimensionality equal to 2");
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
        if (nd == 2)
        {
            this.shape2d = Strel2D.Shape.fromLabel(gd.getNextChoice());
            this.radius  = (int) gd.getNextNumber();
            strel = shape2d.fromRadius(radius);
        }
        else
        {
            this.shape3d = Strel3D.Shape.fromLabel(gd.getNextChoice());
            this.radius  = (int) gd.getNextNumber();
            strel = shape3d.fromRadius(radius);
        }
        
        // create operation
        BinaryMorphologicalFilter algo;
        switch (opIndex)
        {
            case 0: algo = new BinaryDilation(strel); break;
            case 1: algo = new BinaryErosion(strel); break;
            case 2: algo = new BinaryOpening(strel); break;
            case 3: algo = new BinaryClosing(strel); break;
            default:
                throw new RuntimeException("Unknown Operation index");
        }
        
        Image resultImage = imageFrame.runOperator(algo, image);
        
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
