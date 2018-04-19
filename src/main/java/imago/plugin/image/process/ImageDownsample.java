/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.process.shape.Downsampler;
import net.sci.image.Image;

/**
 * Subsample and image with an integer ratio.
 * 
 * @author David Legland
 *
 */
public class ImageDownsample implements Plugin
{
	public ImageDownsample()
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
		System.out.println("downsample");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		
		
		GenericDialog gd = new GenericDialog(frame, "Downsample");
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
		frame.getGui().addNewDocument(result);
	}

}
