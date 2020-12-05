/**
 * 
 */
package imago.plugin.image.shape;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.process.shape.Concatenate;
import net.sci.image.Image;

/**
 * Concatenates two images.
 * 
 * @author David Legland
 *
 */
public class ImageConcatenate implements FramePlugin
{
	public ImageConcatenate()
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
		System.out.println("Image concatenation");

		ImagoGui gui = frame.getGui();
		ImagoApp app = gui.getAppli();
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
		GenericDialog gd = new GenericDialog(frame, "Image Concatenate");
		gd.addChoice("Image 1: ", imageNameArray, firstImageName);
		gd.addChoice("Image 2: ", imageNameArray, secondImageName);
		gd.addNumericField("Dimension: ", 1, 0, "The dimension to concatenate (starting from 1)");
        gd.showDialog();
		
        if (gd.wasCanceled()) 
        {
            return;
        }
		
		// parse dialog results
		Image image1 = app.getImageHandleFromName(gd.getNextChoice()).getImage();
		Image image2 = app.getImageHandleFromName(gd.getNextChoice()).getImage();
		int concatDim = (int) gd.getNextNumber();
        if (concatDim < 1)
        {
            frame.showErrorDialog("Dimension index can not be less than 1");
            return;
        }

        // convert to 0-indexing
        concatDim--;
        
        Array<?> array1 = image1.getData();
        Array<?> array2 = image2.getData();

        Concatenate concat = new Concatenate(concatDim);
        @SuppressWarnings("unchecked")
        Array<?> result = concat.process((Array<Object>) array1, (Array<?>)array2);

        Image resultImage = new Image(result, image1);
		
		// add the image document to GUI
		gui.createImageFrame(resultImage);
	}
}
