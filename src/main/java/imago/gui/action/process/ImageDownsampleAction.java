/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.process.shape.Downsampler;
import net.sci.image.Image;

/**
 * Subsample and image with an integer ratio.
 * 
 * @author David Legland
 *
 */
public class ImageDownsampleAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageDownsampleAction(ImagoFrame frame, String name)
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
		System.out.println("downsample");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(this.frame, "Downsample");
		for (int d = 0; d < nd; d++)
		{
			gd.addNumericField("Ratio dim. " + (d+1), 3, 0);
		}
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		int[] ratioList = new int[nd];
		for (int d = 0; d < nd; d++)
		{
			ratioList[d] = (int) gd.getNextNumber();
		}

		// create operator box filtering operator
		Downsampler resampler = new Downsampler(ratioList); 
		
		// apply operator on current image
		Image result = new Image(resampler.process(array), image);
		result.setName(image.getName() + "-filt");
		
		// add the image document to GUI
		this.gui.addNewDocument(result);
	}

}
