/**
 * 
 */
package imago.gui.action.process;

import java.awt.event.ActionEvent;
import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.data.BinaryArray;
import net.sci.array.data.color.RGB8Array;
import net.sci.array.type.CommonColors;
import net.sci.array.type.RGB8;
import net.sci.image.Image;

/**
 * Applies an overlay on a grayscale or color image using a binary image as mask.
 * 
 * @author David Legland
 *
 */
public class BinaryImageOverlayAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BinaryImageOverlayAction(ImagoFrame frame, String name)
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
		System.out.println("binary image overlay");

		ImagoApp app = this.gui.getAppli();
		Collection<String> imageNames = app.getImageDocumentNames();

		// Case of no open document with image
		if (imageNames.size() == 0)
		{
			return;
		}
		
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];
				
		// Creates the dialog
		GenericDialog gd = new GenericDialog(this.frame, "Binary Overlay");
		gd.addChoice("Reference Image: ", imageNameArray, firstImageName);
		gd.addChoice("Binary Image: ", imageNameArray, firstImageName);
		gd.addChoice("Overlay Color: ", CommonColors.all(), CommonColors.RED);
        gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		Image baseImage = app.getDocumentFromName(gd.getNextChoice()).getImage();
        Image maskImage = app.getDocumentFromName(gd.getNextChoice()).getImage();
        RGB8 color = new RGB8(CommonColors.fromLabel(gd.getNextChoice()).getColor());

		Array<?> baseArray = baseImage.getData();
		Array<?> overlay = maskImage.getData();
		if (!Arrays.isSameDimensionality(baseArray, overlay))
		{
			this.frame.showErrorDialog("Both arrays must have same dimensionality", "Dimensionality Error");
			return;
		}
		
		if (!Arrays.isSameSize(baseArray, overlay))
		{
			this.frame.showErrorDialog("Both arrays must have same size", "Image Size Error");
			return;
		}
		
		if ( !(overlay instanceof BinaryArray) )
		{
			this.frame.showErrorDialog("overlay array should be binary", "Image Type Error");
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
		this.gui.addNewDocument(resultImage);
	}
}
