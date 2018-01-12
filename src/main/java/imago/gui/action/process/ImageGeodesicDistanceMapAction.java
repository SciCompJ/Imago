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
import net.sci.array.data.ScalarArray;
import net.sci.array.data.scalar2d.BinaryArray2D;
import net.sci.array.data.scalar3d.BinaryArray3D;
import net.sci.image.Image;
import net.sci.image.binary.BinaryImages;

/**
 * Computes geodesic distance map of a marker image constrained by a mask image.
 * 
 * @author David Legland
 *
 */
public class ImageGeodesicDistanceMapAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageGeodesicDistanceMapAction(ImagoFrame frame, String name)
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
		System.out.println("geodesic distance map");

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
		GenericDialog gd = new GenericDialog(this.frame, "Geod. Dist. Map");
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
		if (!Arrays.isSameDimensionality(marker, mask))
		{
			this.frame.showErrorDialog("Both arrays must have same dimensionality", "Dimensionality Error");
			return;
		}
		
		if (!Arrays.isSameSize(marker, mask))
		{
			this.frame.showErrorDialog("Both arrays must have same size", "Image Size Error");
			return;
		}
		
		if (!(marker instanceof BinaryArray) || !(mask instanceof BinaryArray) )
		{
			this.frame.showErrorDialog("Both arrays should be binary", "Image Type Error");
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
            this.frame.showErrorDialog("Both arrays should be binary", "Image Type Error");
            System.err.println("Both arrays should be instances of BinaryArray2D or Binary3D");
            return;
		}
		    
		// Create result image
		Image resultImage = new Image(result, markerImage);
		resultImage.setName(markerImage.getName() + "-geodDist");
		
		// add the image document to GUI
		this.gui.addNewDocument(resultImage);
	}
}
