/**
 * 
 */
package imago.plugin.image.process;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.morphology.strel.Strel2D;
import net.sci.image.morphology.strel.Strel3D;

/**
 * Iterates several geodesic dilations of a marker image constrained
 * by a mask image.
 * 
 * Works for both 2D or 3D pairs of images.
 * 
 * @author David Legland
 *
 */
public class ImageIteratedGeodesicDilations implements FramePlugin
{
	public ImageIteratedGeodesicDilations()
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
		GenericDialog gd = new GenericDialog(frame, "Geodesic Dilations");
		gd.addChoice("Marker: ", imageNameArray, firstImageName);
		gd.addChoice("Mask: ", imageNameArray, firstImageName);
		gd.addNumericField("Iterations: ", 3, 0);
        gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		Image markerImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
		Image maskImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
		int nIters = (int) gd.getNextNumber();

		// extract arrays and check dimensions
		Array<?> marker = markerImage.getData();
		Array<?> mask = maskImage.getData();
        if (!Arrays.isSameSize(marker, mask))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays must have same dimensions", "Dimension Error");
            return;
        }
		
        // process morphological reconstruction using default connectivity
        Image resultImage;
        switch(markerImage.getDimension())
        {
        case 2:
        {
            // convert to 2D arrays
            ScalarArray2D<?> mask2d = ScalarArray2D.wrapScalar2d((ScalarArray<?>) mask);
            ScalarArray2D<?> array = ScalarArray2D.wrapScalar2d((ScalarArray<?>) marker);
            // create structuring element corresponding to 8-connectivity
            Strel2D strel = Strel2D.Shape.SQUARE.fromDiameter(3);
            
            // iterate dilations
            for (int iIter = 0; iIter < nIters; iIter++)
            {
                System.out.println("iter " + iIter);
                array = (ScalarArray2D<?>) strel.dilation(array);
                net.sci.array.process.Math.min(array, mask2d, array);
            }
            
            resultImage = new Image(array, maskImage);
            break;
        }   
        case 3:
        {
            // convert to 2D arrays
            ScalarArray3D<?> mask3d = ScalarArray3D.wrapScalar3d((ScalarArray<?>) mask);
            ScalarArray3D<?> array = ScalarArray3D.wrapScalar3d((ScalarArray<?>) marker);
            // create structuring element corresponding to 26-connectivity
            Strel3D strel = Strel3D.Shape.CUBE.fromDiameter(3);
            
            // iterate dilations
            for (int iIter = 0; iIter < nIters; iIter++)
            {
                System.out.println("iter " + iIter);
                array = (ScalarArray3D<?>) strel.dilation(array);
                net.sci.array.process.Math.min(array, mask3d, array);
            }
            
            resultImage = new Image(array, maskImage);
            break;
        }
        default:
            ImagoGui.showErrorDialog(frame, "Can only manage dimensions 2 or 3", "Dimension Error");
            return;
        }

        resultImage.setName(markerImage.getName() + "-geodDil");
		
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}
}
