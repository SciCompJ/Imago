/**
 * 
 */
package imago.plugin.image.shape;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.process.shape.SubSample;
import net.sci.image.Image;

/**
 * Subsample and image with an integer ratio.
 * 
 * @author David Legland
 *
 */
public class ImageSubsample implements FramePlugin
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
        ImageFrame iFrame = (ImageFrame) frame;
        Image image = iFrame.getImageHandle().getImage();
		Array<?> array = image.getData();

		GenericDialog gd = new GenericDialog(frame, "Subsample");
        gd.addNumericField("Sampling Step", 2, 0);
        gd.addNumericField("Origin Shift", 0, 0);
		
		gd.showDialog();
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
        int step = (int) gd.getNextNumber();
        int shift = (int) gd.getNextNumber();
		int nd = array.dimensionality();
        int[] steps = repeatValue(step, nd);
        int[] origins = repeatValue(shift, nd);
		        
		// create operator box filtering operator
		SubSample resampler = new SubSample(steps, origins); 
		
		// apply operator on current image
		Image result = new Image(resampler.process(array), image);
		result.setName(image.getName() + "-sub" + step);
		
		// add the image document to GUI
		ImageFrame frame2 = iFrame.createImageFrame(result);
		
        // choose z-position approximately the same as original image
        int zPos = iFrame.getImageView().getSlicingPosition(2);
        double relPos = ((double) zPos) / ((double) image.getSize(2));
        zPos = (int) Math.floor(result.getSize(2) * relPos);
        frame2.getImageView().setSlicingPosition(2, zPos);
        frame2.getImageView().refreshDisplay();
	}

    private static final int[] repeatValue(int value, int n)
    {
        int[] res = new int[n];
        for (int i = 0; i < n; i++)
        {
            res[i] = value;
        }
        return res;
    }
}
