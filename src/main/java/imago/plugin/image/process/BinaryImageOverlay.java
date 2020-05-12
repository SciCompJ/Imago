/**
 * 
 */
package imago.plugin.image.process;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.color.CommonColors;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.scalar.BinaryArray;
import net.sci.image.Image;

/**
 * Applies an overlay on a grayscale or color image using a binary image as mask.
 * 
 * @author David Legland
 *
 */
public class BinaryImageOverlay implements Plugin
{
	public BinaryImageOverlay()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
    {
		System.out.println("binary image overlay");

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
		gd.addChoice("Overlay Color: ", CommonColors.all(), CommonColors.RED);
        gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		Image baseImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
        Image maskImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
        RGB8 color = new RGB8(CommonColors.fromLabel(gd.getNextChoice()).getColor());

		Array<?> baseArray = baseImage.getData();
		Array<?> overlay = maskImage.getData();
		if (!Arrays.isSameDimensionality(baseArray, overlay))
		{
			frame.showErrorDialog("Both arrays must have same dimensionality", "Dimensionality Error");
			return;
		}
		
		if (!Arrays.isSameSize(baseArray, overlay))
		{
			frame.showErrorDialog("Both arrays must have same size", "Image Size Error");
			return;
		}
		
		if ( !(overlay instanceof BinaryArray) )
		{
			frame.showErrorDialog("overlay array should be binary", "Image Type Error");
			return;
		}
		
		
		// Create the RGB8 result array
		RGB8Array result;
		if (baseArray instanceof RGB8Array) 
		    result = ((RGB8Array) baseArray).duplicate();
		else
		    result = RGB8Array.convert(baseArray);
		
		// compute overlay
		RGB8Array.binaryOverlay(result, (BinaryArray) overlay, color);
		
		// Create result image
		Image resultImage = new Image(result, baseImage);
		resultImage.setName(baseImage.getName() + "-ovr");
		
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage);
	}
}
