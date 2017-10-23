/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;
import java.util.Collection;

import net.sci.array.Array;
import net.sci.array.data.BinaryArray;
import net.sci.array.data.scalar2d.BinaryArray2D;
import net.sci.array.data.scalar2d.ScalarArray2D;
import net.sci.image.Image;
import net.sci.image.binary.geoddist.GeodesicDistanceTransform2D;
import net.sci.image.binary.geoddist.GeodesicDistanceTransform2DFloat5x5Scanning;

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
		if (!Array.isSameDimensionality(marker, mask))
		{
			this.frame.showErrorDialog("Both arrays must have same dimensionality", "Dimensionality Error");
			return;
		}
		
		if (!Array.isSameSize(marker, mask))
		{
			this.frame.showErrorDialog("Both arrays must have same size", "Image Size Error");
			return;
		}
		
		if (marker.dimensionality() != 2)
		{
			this.frame.showErrorDialog("Require array with dimensionality 2", "Dimensionality Error");
			return;
		}
		
		if (!(marker instanceof BinaryArray) || !(mask instanceof BinaryArray) )
		{
			this.frame.showErrorDialog("Both arrays should be binary", "Image Type Error");
			return;
		}
		
		if (!(marker instanceof BinaryArray2D) || !(mask instanceof BinaryArray2D) )
		{
            this.frame.showErrorDialog("Both arrays should be binary", "Image Type Error");
			System.err.println("Both arrays should be instances of BinaryArray2D");
			return;
		}
		
		// Create operator, and computes distance map
		GeodesicDistanceTransform2D op = new GeodesicDistanceTransform2DFloat5x5Scanning();
		ScalarArray2D<?> result = op.process((BinaryArray2D) marker, (BinaryArray2D) mask);
		
		// Create result image
		Image resultImage = new Image(result, markerImage);
		resultImage.setName(markerImage.getName() + "-geodDist");
		
		// add the image document to GUI
		this.gui.addNewDocument(resultImage);
	}
}
