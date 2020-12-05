/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.process.shape.SubSampler;
import net.sci.image.Image;

/**
 * Subsample and image with an integer ratio.
 * 
 * @author David Legland
 *
 */
public class ImageSubsample implements Plugin
{
	public ImageSubsample()
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
		System.out.println("subsample");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		GenericDialog gd = new GenericDialog(frame, "Subsample");
		gd.addNumericField("Subsampling ratio", 2, 0);
		
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		int ratio = (int) gd.getNextNumber();
		
		// create operator box filtering operator
		SubSampler resampler = new SubSampler(ratio); 
		
		// apply operator on current image
		Image result = new Image(resampler.process(array), image);
		result.setName(image.getName() + "-sub" + ratio);
		
		// add the image document to GUI
		frame.getGui().createImageFrame(result);
	}

}
