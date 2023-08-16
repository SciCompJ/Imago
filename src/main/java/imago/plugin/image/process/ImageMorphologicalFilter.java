/**
 * 
 */
package imago.plugin.image.process;

import java.util.Locale;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalFilters.Operation;
import net.sci.image.morphology.MorphologicalFilter;
import net.sci.image.morphology.Strel;
import net.sci.image.morphology.strel.Strel2D;
import net.sci.image.morphology.strel.Strel3D;

/**
 * Applies various types of morphological filtering on a 2D or 3D image.
 * 
 * @author David Legland
 *
 */
public class ImageMorphologicalFilter implements FramePlugin
{
    // =============================================================
    // Class variables
    
   /**
     * The operation to apply to the image.
     */
	Operation op = Operation.DILATION;

	/**
     * The shape of the structuring element for 2D images.
     */
    Strel2D.Shape shape2d = Strel2D.Shape.SQUARE;

    /**
     * The shape of the structuring element for 3D images.
     */
    Strel3D.Shape shape3d = Strel3D.Shape.CUBE;
    
    /**
     * The radius of the structuring element, in pixels.
     */
	int radius = 2;

	
    // =============================================================
    // Implements Plugin interface
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current image data
		ImageFrame imageFrame = (ImageFrame) frame;
		Image image	= imageFrame.getImageHandle().getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		if (nd != 2 && nd != 3)
		{
			System.err.println("Requires image of dimensionality equal to 2 or 3");
			return;
		}
		
		// Create dialog for entering parameters
		GenericDialog gd = new GenericDialog(frame, "Morphological Filter");
		gd.addEnumChoice("Operation", Operation.class, this.op);
        if (nd == 2)
		{
            gd.addEnumChoice("Element", Strel2D.Shape.class, this.shape2d);
	        gd.addNumericField("Radius (in pixels)", this.radius, 0);
		}
		else
		{
            gd.addEnumChoice("Element", Strel3D.Shape.class, this.shape3d);
	        gd.addNumericField("Radius (in voxels)", this.radius, 0);
        }
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		// extract chosen parameters
        this.op = (Operation) gd.getNextEnumChoice();
		String shapeSuffix = "";
		Strel strel;
		if (nd == 2)
		{
            this.shape2d = (Strel2D.Shape) gd.getNextEnumChoice();
            shapeSuffix = this.shape2d.suffix();
	        this.radius  = (int) gd.getNextNumber();
		    strel = shape2d.fromRadius(radius);
		}
		else
		{
            this.shape3d = (Strel3D.Shape) gd.getNextEnumChoice();
            shapeSuffix = this.shape3d.suffix();
	        this.radius  = (int) gd.getNextNumber();
		    strel = shape3d.fromRadius(radius);
		}

		// Create the morphological filter
		MorphologicalFilter algo = op.createOperator(strel);
		
        // Execute core of the plugin on the array of original image
		Image resultImage = imageFrame.runOperator(algo, image);

		// setup name of result image
        String suffix = String.format(Locale.ENGLISH, "%s%s%02d", op.suffix(), shapeSuffix, radius);
		resultImage.setName(image.getName() + "-" + suffix);
		
		// add the image document to GUI
		frame.createImageFrame(resultImage);
	}
}
