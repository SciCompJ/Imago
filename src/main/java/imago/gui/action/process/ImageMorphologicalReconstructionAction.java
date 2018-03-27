/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;

import java.awt.event.ActionEvent;
import java.util.Collection;

import net.sci.array.Array;
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
public class ImageMorphologicalReconstructionAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageMorphologicalReconstructionAction(ImagoFrame frame, String name)
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
	public void actionPerformed(ActionEvent arg0)
	{
		System.out.println("morphological reconstruction");

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
		GenericDialog gd = new GenericDialog(this.frame, "Morpho. Rec.");
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
		if (marker.dimensionality() != mask.dimensionality())
		{
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimension", "Dimension Error");
			return;
		}
		
		Image resultImage = MorphologicalReconstruction.reconstructByDilation(markerImage, maskImage);
		resultImage.setName(markerImage.getName() + "-morphoRec");
		
		// add the image document to GUI
		this.gui.addNewDocument(resultImage);
	}
}
