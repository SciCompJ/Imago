/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalFilters.Operation;
import net.sci.image.morphology.strel.Strel3D;

/**
 * Applies various types of morphological filtering on a multidimensional image.
 * 
 * @deprecated: superseded by ImageMorphologicalFilter class
 * @author David Legland
 * 
 */
@Deprecated 
public class ImageMorphologicalFilter3D implements FramePlugin
{
	Operation op = Operation.DILATION;
	Strel3D.Shape shape = Strel3D.Shape.CUBE;
	int radius = 2;
	boolean showStrel;

	public ImageMorphologicalFilter3D()
	{
	}

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
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		if (nd != 3)
		{
			System.err.println("Requires image of dimensionality equal to 3");
			return;
		}
		
		GenericDialog gd = new GenericDialog(frame, "Morphological Filter");
		gd.addChoice("Operation", Operation.getAllLabels(), 
				this.op.toString());
		gd.addChoice("Element", Strel3D.Shape.getAllLabels(), 
				this.shape.toString());
		gd.addNumericField("Radius (in pixels)", this.radius, 0);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		// extract chosen parameters
		this.op 		= Operation.fromLabel(gd.getNextChoice());
		this.shape 		= Strel3D.Shape.fromLabel(gd.getNextChoice());
		this.radius 	= (int) gd.getNextNumber();		

		// Create structuring element of the given shape and size
		Strel3D strel = shape.fromRadius(radius);
		
		// add some listeners
		strel.addAlgoListener((ImageFrame) frame); 
		
		// Execute core of the plugin on the original image
		Array<?> result = op.process((ScalarArray3D<?>) array, strel);
		
		// create new image with filter result
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-filt");
		
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}
}
