/**
 * 
 */
package imago.plugin.image.binary;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.color.BinaryOverlayRGB8Array;
import net.sci.array.color.CommonColors;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.image.Image;

/**
 * Applies an overlay on a grayscale or color image using a binary image as mask.
 * 
 * @author David Legland
 *
 */
public class BinaryImageOverlay implements FramePlugin
{
	@Override
    public void run(ImagoFrame frame, String args)
    {
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
		gd.addChoice("Binary Mask: ", imageNameArray, secondImageName);
        gd.addEnumChoice("Overlay Color: ", CommonColors.class, CommonColors.RED);
        gd.addNumericField("Overlay Opacity:", 50, 0, "The opacity of the binary overlay, between 0 and 100");
        gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		Image baseImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
        Image maskImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
        RGB8 color = (RGB8) ((CommonColors) gd.getNextEnumChoice()).getColor();
        double opacity = Math.max(Math.min(gd.getNextNumber(), 100.0), 0.0) / 100.0;

        // retrieve image data
		Array<?> baseArray = baseImage.getData();
		Array<?> binaryMask = maskImage.getData();
		
		// check validity of input images
		if (!Arrays.isSameDimensionality(baseArray, binaryMask))
		{
			frame.showErrorDialog("Both arrays must have same dimensionality", "Dimensionality Error");
			return;
		}
		if (!Arrays.isSameSize(baseArray, binaryMask))
		{
			frame.showErrorDialog("Both arrays must have same size", "Image Size Error");
			return;
		}
        if (binaryMask.dataType() != Binary.class)
		{
			frame.showErrorDialog("overlay array must be binary", "Image Type Error");
			return;
		}
		
		// create RGB8 array as a view
		RGB8Array result = new BinaryOverlayRGB8Array(baseArray, BinaryArray.wrap(binaryMask), color, opacity);
		
		// Create result image
		Image resultImage = new Image(result, baseImage);
		resultImage.setName(baseImage.getName() + "-ovr");
		
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}
}
