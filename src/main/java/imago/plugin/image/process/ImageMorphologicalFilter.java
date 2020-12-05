/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalFilter.Operation;
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
		System.out.println("morphological filter (2d)");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		if (nd != 2 && nd != 3)
		{
			System.err.println("Requires image of dimensionality equal to 2 or 3");
			return;
		}
		
		// Create dialog for entering parameters
		GenericDialog gd = new GenericDialog(frame, "Morphological Filter");
		gd.addChoice("Operation", Operation.getAllLabels(), 
				this.op.toString());
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
		this.op 		= Operation.fromLabel(gd.getNextChoice());
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

		// add listener
		strel.addAlgoListener((ImageFrame) frame); 
		
		// Execute core of the plugin on the array of original image
		Array<?> result = op.process(array, strel);
		
		// determine operation short name
		String opName;
		switch (op)
		{
        case DILATION: opName = "Dil"; break;
        case EROSION: opName = "Ero"; break;
        case OPENING: opName = "Op"; break;
        case CLOSING: opName = "Cl"; break;
        case TOPHAT: opName = "WTH"; break;
        case BOTTOMHAT: opName = "BTH"; break;
        case GRADIENT: opName = "Grad"; break;
        case LAPLACIAN: opName = "Lap"; break;
        
        default: opName = "Filt"; break;
		}

		// create new image with filter result
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-" + opName);
		
		// add the image document to GUI
		frame.createImageFrame(resultImage);
	}

}
