/**
 * 
 */
package imago.image.plugin.process;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.array.numeric.IntArray;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.array.numeric.UInt8Array;
import net.sci.image.Image;
import net.sci.image.ImageType;
import net.sci.image.connectivity.Connectivity2D;
import net.sci.image.connectivity.Connectivity3D;
import net.sci.image.morphology.MinimaAndMaxima;
import net.sci.image.morphology.watershed.Watershed2D;
import net.sci.image.morphology.watershed.Watershed3D;

/**
 * Computes watershed of a scalar image resulting in set of catchment basins
 * separated by a watershed.
 * 
 * @author David Legland
 *
 */
public class ImageWatershed implements FramePlugin
{
	public ImageWatershed()
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
		// get current image data
        ImageFrame iFrame = (ImageFrame) frame;
		Image image	= iFrame.getImageHandle().getImage();
		
		// current dimensionality
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar array");
		}
		int nd = array.dimensionality();
		
        // String lists for dialog widgets
        String[] connectivityNames = switch (nd)
        {
            case 2 -> new String[] { "C4", "C8" };
            case 3 -> new String[] { "C6", "C26" };
            default -> new String[] { "Ortho", "Full" };
        };
        
		// Computes max possible value for dynamic
		double maxValue = 255;
		if (!(array instanceof UInt8Array))
		{
			double[] range = ((ScalarArray<?>) array).finiteValueRange();
			maxValue = range[1] - range[0];
		}

		// Creates generic dialog
		GenericDialog gd = new GenericDialog(frame, "Extended Min./Max.");
		gd.addSlider("Dynamic: ", 0, maxValue, 10);
		gd.addChoice("Connectivity: ", connectivityNames, connectivityNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		double dynamic = gd.getNextNumber();
		int connIndex = gd.getNextChoiceIndex();
				
		IntArray<?> result;
		if (nd == 2)
		{
		    ScalarArray2D<?> array2d = (ScalarArray2D<?>) ScalarArray2D.wrapScalar2d((ScalarArray<?>) array);
			Connectivity2D conn = connIndex == 0 ? Connectivity2D.C4 : Connectivity2D.C8;
			BinaryArray2D minima = MinimaAndMaxima.extendedMinima2d(array2d, dynamic, conn);
			ScalarArray2D<?> imposed = MinimaAndMaxima.imposeMinima2d(array2d, minima, conn);
			result = new Watershed2D(conn).process(imposed);
		}
		else if (nd == 3)
		{
		    ScalarArray3D<?> array3d = (ScalarArray3D<?>) ScalarArray3D.wrapScalar3d((ScalarArray<?>) array);
		    Connectivity3D conn = connIndex == 0 ? Connectivity3D.C6 : Connectivity3D.C26;
		    BinaryArray3D minima = MinimaAndMaxima.extendedMinima3d(array3d, dynamic, conn);
		    ScalarArray3D<?> imposed = MinimaAndMaxima.imposeMinima3d(array3d, minima, conn);
		    result = new Watershed3D(conn).process(imposed);
		}
		else
		{
			System.err.println("Unable to process array with dimensionality " + nd);
			return;
		}
		
		// apply operator on current image
		Image resultImage = new Image(result, ImageType.LABEL, image);
		
		// choose appropriate suffix
		String suffix = "-wat";
		resultImage.setName(image.getName() + suffix);
		
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}

	/**
     * Returns true if the current frame contains a scalar image.
     * 
     * @param frame
     *            the frame from which the plugin is called
     * @return true if the frame contains a scalar image
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return true;
    }
}
