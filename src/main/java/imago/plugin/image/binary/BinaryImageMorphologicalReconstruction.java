/**
 * 
 */
package imago.plugin.image.binary;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.image.Image;
import net.sci.image.morphology.reconstruct.RunLengthBinaryReconstruction2D;
import net.sci.image.morphology.reconstruct.RunLengthBinaryReconstruction3D;

/**
 * Applies binary morphological reconstruction of a marker image constrained by a mask image.
 * 
 * Works for 2D pairs of images.
 * 
 * @author David Legland
 *
 */
public class BinaryImageMorphologicalReconstruction implements FramePlugin
{
	public BinaryImageMorphologicalReconstruction()
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
		System.out.println("binary reconstruction");

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
				
		// Creates the dialog
		GenericDialog gd = new GenericDialog(frame, "Morpho. Rec.");
		gd.addChoice("Marker: ", imageNameArray, firstImageName);
		gd.addChoice("Mask: ", imageNameArray, firstImageName);
		gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		Image markerImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
		Image maskImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();

		// extract arrays and check dimensions
		Array<?> marker = markerImage.getData();
		Array<?> mask = maskImage.getData();
        if (!Arrays.isSameSize(marker, mask))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimensions", "Dimension Error");
            return;
        }
		
        // check arrays are binary
        if (!(marker instanceof BinaryArray) || !(mask instanceof BinaryArray))
        {
            ImagoGui.showErrorDialog(frame, "Input arrays must be binary", "Array Type Error");
            return;
        }
        BinaryArray result;
        
        if (marker.dimensionality() == 2 && mask.dimensionality() == 2)
        {
            RunLengthBinaryReconstruction2D algo = new RunLengthBinaryReconstruction2D();
            result = algo.processBinary2d(BinaryArray2D.wrap((BinaryArray) marker), BinaryArray2D.wrap((BinaryArray) mask));
        }
        else if (marker.dimensionality() == 3 && mask.dimensionality() == 3)
        {
            RunLengthBinaryReconstruction3D algo = new RunLengthBinaryReconstruction3D();
            result = algo.processBinary3d(BinaryArray3D.wrap((BinaryArray) marker), BinaryArray3D.wrap((BinaryArray) mask));
        }
        else
        {
            throw new RuntimeException("Can process onlt dimensions 2 or 3.");
        }
        
        // encapsulate into image
		Image resultImage = new Image(result, maskImage);
		resultImage.setName(markerImage.getName() + "-morphoRec");
		
		// add the image document to GUI
		gui.createImageFrame(resultImage);
	}
}
