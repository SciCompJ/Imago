/**
 * 
 */
package imago.plugin.image.binary;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.BinaryArray;
import net.sci.array.process.binary.BinaryMask;
import net.sci.image.Image;

/**
 * Computes a new image the same size and the same type as an input image, by
 * retaining values specified by a binary mask.
 * 
 * @author dlegland
 */
public class ApplyBinaryMask implements FramePlugin
{
	public ApplyBinaryMask()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
    {
		System.out.println("apply binary mask");

		ImagoApp app = frame.getGui().getAppli();
		Collection<String> imageNames = app.getImageHandleNames();

		// Case of no open document with image
		if (imageNames.size() == 0)
		{
			return;
		}
		
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];
		String secondImageName = imageNameArray[Math.min(1, imageNameArray.length-1)];
	
		// Creates the dialog
		GenericDialog gd = new GenericDialog(frame, "Binary Overlay");
		gd.addChoice("Reference Image: ", imageNameArray, firstImageName);
		gd.addChoice("Binary Image: ", imageNameArray, secondImageName);
        gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		Image baseImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
        Image maskImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();

        // retrieve arrays
		Array<?> array = baseImage.getData();
		Array<?> mask = maskImage.getData();
		
		// check input validity
		if (!Arrays.isSameDimensionality(array, mask))
		{
			frame.showErrorDialog("Both images must have same dimensionality", "Dimensionality Error");
			return;
		}
		if (!Arrays.isSameSize(array, mask))
		{
			frame.showErrorDialog("Both images must have same size", "Image Size Error");
			return;
		}
		if ( !(mask instanceof BinaryArray) )
		{
			frame.showErrorDialog("Mask image must be binary", "Image Type Error");
			return;
		}
		
		// combine array with mask
		BinaryMask op = new BinaryMask();
		Array<?> result = op.process(array, BinaryArray.wrap(mask));
		
		// Create result image
		Image resultImage = new Image(result, baseImage);
		resultImage.setName(baseImage.getName() + "-mask");
		
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage, frame);
	}
}
