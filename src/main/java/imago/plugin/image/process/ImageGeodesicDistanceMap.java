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
import net.sci.array.scalar.BinaryArray;
import net.sci.array.scalar.BinaryArray2D;
import net.sci.array.scalar.BinaryArray3D;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;
import net.sci.image.binary.BinaryImages;

/**
 * Computes geodesic distance map of a marker image constrained by a mask image.
 * 
 * @author David Legland
 *
 */
public class ImageGeodesicDistanceMap implements Plugin
{
	public ImageGeodesicDistanceMap()
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
		System.out.println("geodesic distance map");

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
		GenericDialog gd = new GenericDialog(frame, "Geod. Dist. Map");
		gd.addChoice("Marker: ", imageNameArray, firstImageName);
		gd.addChoice("Mask: ", imageNameArray, firstImageName);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		Image markerImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
		Image maskImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();

		Array<?> marker = markerImage.getData();
		Array<?> mask = maskImage.getData();
		if (!Arrays.isSameDimensionality(marker, mask))
		{
			frame.showErrorDialog("Both arrays must have same dimensionality", "Dimensionality Error");
			return;
		}
		
		if (!Arrays.isSameSize(marker, mask))
		{
			frame.showErrorDialog("Both arrays must have same size", "Image Size Error");
			return;
		}
		
		if (!(marker instanceof BinaryArray) || !(mask instanceof BinaryArray) )
		{
			frame.showErrorDialog("Both arrays should be binary", "Image Type Error");
			return;
		}
		
		ScalarArray<?> result;
		if (marker instanceof BinaryArray2D && mask instanceof BinaryArray2D) 
		{
    		// computes distance map for 2D images
    		result = BinaryImages.geodesicDistanceMap2d((BinaryArray2D) marker, (BinaryArray2D) mask);
		}
		else if (marker instanceof BinaryArray3D && mask instanceof BinaryArray3D)
		{
            // computes distance map for 3D images
            result = BinaryImages.geodesicDistanceMap3d((BinaryArray3D) marker, (BinaryArray3D) mask);
		}
		else
		{
            frame.showErrorDialog("Both arrays should be binary", "Image Type Error");
            System.err.println("Both arrays should be instances of BinaryArray2D or Binary3D");
            return;
		}
		    
		// Create result image
		Image resultImage = new Image(result, markerImage);
		resultImage.setName(markerImage.getName() + "-geodDist");
		
		// add the image document to GUI
		gui.addNewDocument(resultImage);
	}
}
