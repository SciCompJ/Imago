/**
 * 
 */
package imago.plugin.image.shape;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.process.numeric.DownSample;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;

/**
 * Smooth and Subsample and image with an integer ratio.
 * 
 * @author David Legland
 *
 */
public class ImageDownsample implements FramePlugin
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
		// get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        Image image = iFrame.getImageHandle().getImage();
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
		    frame.showErrorDialog("Requires a scalar image as input");
            return;
		}

		GenericDialog gd = new GenericDialog(frame, "DownSample");
        gd.addNumericField("Downsampling factor", 2, 0);
		
		gd.showDialog();
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
        int factor = (int) gd.getNextNumber();
		        
		// create operator box filtering operator
		DownSample resampler = new DownSample(factor);
		
		// apply operator on current image
//		Image result = new Image(resampler.process(array), image);
		Image result = iFrame.runOperator(resampler, image);
		result.setName(image.getName() + "-down" + factor);
		
		// add the image document to GUI
        ImageFrame frame2 = ImageFrame.create(result, frame);
		if (image.getDimension() > 2)
		{
		    int currentSlice = iFrame.getImageView().getSlicingPosition(2);
		    int newSlice = currentSlice / factor;
		    frame2.getImageView().setSlicingPosition(2, newSlice);
		}
	}
}
