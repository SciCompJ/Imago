/**
 * 
 */
package imago.plugin.image.process;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalReconstruction;

/**
 * Applies morphological reconstruction of a marker image constrained by a mask image.
 * 
 * Works for both 2D or 3D pairs of images.
 * 
 * @author David Legland
 *
 */
public class ImageMorphologicalReconstruction implements Plugin
{
	public ImageMorphologicalReconstruction()
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
		System.out.println("morphological reconstruction");

		ImagoGui gui = frame.getGui();
		ImagoApp app = gui.getAppli();
		Collection<String> imageNames = app.getImageDocumentNames();

		// Case of no open document with image
		if (imageNames.size() == 0)
		{
			return;
		}
		
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];
				
		// Creates the dialog
		GenericDialog gd = new GenericDialog(frame, "Morpho. Rec.");
		gd.addChoice("Marker: ", imageNameArray, firstImageName);
		gd.addChoice("Mask: ", imageNameArray, firstImageName);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		Image markerImage = app.getDocumentFromName(gd.getNextChoice()).getImage();
		Image maskImage = app.getDocumentFromName(gd.getNextChoice()).getImage();

		Array<?> marker = markerImage.getData();
		Array<?> mask = maskImage.getData();
        if (!Arrays.isSameSize(marker, mask))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimensions", "Dimension Error");
            return;
        }
		
		Image resultImage = MorphologicalReconstruction.reconstructByDilation(markerImage, maskImage);
		resultImage.setName(markerImage.getName() + "-morphoRec");
		
		// add the image document to GUI
		gui.addNewDocument(resultImage);
	}
}
