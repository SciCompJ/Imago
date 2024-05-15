/**
 * 
 */
package imago.plugin.image.process;

import java.util.Collection;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.Scalar;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.image.Connectivity2D;
import net.sci.image.Connectivity3D;
import net.sci.image.Image;
import net.sci.image.morphology.MinimaAndMaxima;

/**
 * Impose binary minima or maxima on a scalar image.
 * 
 * Works for both 2D or 3D pairs of images.
 * 
 * @author David Legland
 *
 */
public class ImageImposeExtrema implements FramePlugin
{
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
        Collection<String> imageNames = ImageHandle.getAllNames(app);

		// Case of no open document with image
		if (imageNames.size() == 0)
		{
			return;
		}
		
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];

		// String lists for dialog widgets
        String[] operationNames = new String[]{"Impose Minima", "Impose Maxima"};
        String[] connectivityNames = new String[]{"Ortho", "Full"};

				
		// Creates the dialog
		GenericDialog gd = new GenericDialog(frame, "Impose Minima/Maxima");
		gd.addChoice("Image: ", imageNameArray, firstImageName);
		gd.addChoice("Minima Image: ", imageNameArray, firstImageName);
        gd.addChoice("Operation", operationNames, operationNames[0]);
        gd.addChoice("Connectivity: ", connectivityNames, connectivityNames[0]);
		gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		Image refImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
		Image markerImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        int opIndex = gd.getNextChoiceIndex();
        int connIndex = gd.getNextChoiceIndex();

		// extract arrays and check dimensions
		Array<?> array = refImage.getData();
		Array<?> marker = markerImage.getData();
        if (!Arrays.isSameSize(array, marker))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimensions", "Image Size Error");
            return;
        }
		
        // declare result
        Array<?> res;
       
        // switch processing depending on dimension
        int ndims = array.dimensionality();
        if (ndims == 2)
        {
            // convert to 2D data
            @SuppressWarnings({ "unchecked", "rawtypes" })
            ScalarArray2D<?> array2d = ScalarArray2D.wrapScalar2d(ScalarArray.wrap((Array<? extends Scalar>) array));
            @SuppressWarnings("unchecked")
            BinaryArray2D marker2d = BinaryArray2D.wrap(BinaryArray.wrap((Array<Binary>) marker));
            Connectivity2D conn2d = connIndex == 0 ? Connectivity2D.C4 : Connectivity2D.C8;
            
            // minima or maxima imposition
            if (opIndex == 0)
            {
                res = MinimaAndMaxima.imposeMinima(array2d, marker2d, conn2d);
            }
            else
            {
                res = MinimaAndMaxima.imposeMaxima(array2d, marker2d, conn2d);
            }
        }
        else if (ndims == 3)
        {
            // convert to 3D data
            @SuppressWarnings({ "unchecked", "rawtypes" })
            ScalarArray3D<?> array3d = ScalarArray3D.wrapScalar3d(ScalarArray.wrap((Array<? extends Scalar>) array));
            @SuppressWarnings("unchecked")
            BinaryArray3D marker3d = BinaryArray3D.wrap(BinaryArray.wrap((Array<Binary>) marker));
            Connectivity3D conn3d = connIndex == 0 ? Connectivity3D.C6 : Connectivity3D.C26;
            
            // minima or maxima imposition
            if (opIndex == 0)
            {
                res = MinimaAndMaxima.imposeMinima(array3d, marker3d, conn3d);
            }
            else
            {
                res = MinimaAndMaxima.imposeMaxima(array3d, marker3d, conn3d);
            }
        }
        else
        {
            ImagoGui.showErrorDialog(frame, "Min/Max imposition not implemented for dimension " + ndims, "Unimplemented Operation");
            return;
        }
        
        
		Image resultImage = new Image(res, refImage);
		resultImage.setName(refImage.getName() + (opIndex == 0 ? "-imposeMin" : "-imposeMax"));
		
		// add the image document to GUI
		ImageFrame.create(resultImage, frame);
	}
}
