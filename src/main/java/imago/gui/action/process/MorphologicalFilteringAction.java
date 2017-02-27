/**
 * 
 */
package imago.gui.action.process;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.data.Array2D;
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalFiltering.Operation;
import net.sci.image.morphology.Strel2D;

/**
 * Applies various types of morphological filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class MorphologicalFilteringAction extends ImagoAction
{
	Operation op = Operation.DILATION;
	Strel2D.Shape shape = Strel2D.Shape.SQUARE;
	int radius = 2;
	boolean showStrel;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MorphologicalFilteringAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("morphological filter (2d)");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		if (nd != 2)
		{
			System.err.println("Requires image of dimensionality equal to 2");
			return;
		}
		
		GenericDialog gd = new GenericDialog(this.frame, "Morphological Filter");
		gd.addChoice("Operation", Operation.getAllLabels(), 
				this.op.toString());
		gd.addChoice("Element", Strel2D.Shape.getAllLabels(), 
				this.shape.toString());
		gd.addNumericField("Radius (in pixels)", this.radius, 0);
//		gd.addCheckbox("Show Element", false);
//		gd.addPreviewCheckbox(pfr);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		// extract chosen parameters
		this.op 		= Operation.fromLabel(gd.getNextChoice());
		this.shape 		= Strel2D.Shape.fromLabel(gd.getNextChoice());
		this.radius 	= (int) gd.getNextNumber();		
//		this.showStrel 	= gd.getNextBoolean();
//		this.previewing = gd.getPreviewCheckbox().getState();

		// Create structuring element of the given shape and size
		Strel2D strel = shape.fromRadius(radius);
		
//		// add some listeners
//		DefaultAlgoListener.monitor(strel);
		
//		// Eventually display the structuring element used for processing 
//		if (showStrel) 
//		{
//			showStrelImage(strel);
//		}
		
		// Execute core of the plugin on the original image
		Array<?> result = op.apply((Array2D<?>) array, strel);
//		if (!(result instanceof ColorProcessor))
//			result.setLut(this.baseImage.getLut());

//    	if (previewing) 
//    	{
//    		// Fill up the values of original image with values of the result
//    		for (int i = 0; i < image.getPixelCount(); i++)
//    		{
//    			image.setf(i, result.getf(i));
//    		}
//    		image.resetMinAndMax();
//        }
		
		// apply operator on current image
		Image resultImage = new Image(result, image);
		resultImage.setName(image.getName() + "-filt");
		
		// add the image document to GUI
		this.gui.addNewDocument(resultImage);
	}

}
