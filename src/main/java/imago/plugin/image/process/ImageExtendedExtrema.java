/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;
import net.sci.image.data.Connectivity2D;
import net.sci.image.morphology.MinimaAndMaxima;

/**
 * Computes extended minima or maxima on a scalar image.
 * 
 * @author David Legland
 *
 */
public class ImageExtendedExtrema implements FramePlugin
{
	public ImageExtendedExtrema()
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
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		
		// current dimensionality
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar array");
		}
		int nd = array.dimensionality();
		
		// String lists for dialog widgets
		String[] operationNames = new String[]{"Extended Minima", "Extended Maxima"};
		String[] connectivityNames = new String[]{"Ortho", "Full"};

		// Computes max possible value for dynamic
		double maxValue = 255;
		if (!(array instanceof UInt8Array))
		{
			double[] range = ((ScalarArray<?>) array).valueRange();
			maxValue = range[1] - range[0];
		}

		// Creates generic dialog
		GenericDialog gd = new GenericDialog(frame, "Extended Min./Max.");
		gd.addChoice("Operation", operationNames, operationNames[0]);
		gd.addSlider("Dynamic: ", 0, maxValue, 10);
		gd.addChoice("Connectivity: ", connectivityNames, connectivityNames[0]);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		int opIndex = gd.getNextChoiceIndex();
		double dynamic = gd.getNextNumber();
		int connIndex = gd.getNextChoiceIndex();
				
		Array<?> result;
		if (nd == 2)
		{
			Connectivity2D conn = connIndex == 0 ? Connectivity2D.C4 : Connectivity2D.C8;
			if (opIndex == 0)
				result = MinimaAndMaxima.extendedMinima((ScalarArray2D<?>) array, dynamic, conn);
			else
				result = MinimaAndMaxima.extendedMaxima((ScalarArray2D<?>) array, dynamic, conn);
		}
		else
		{
			System.err.println("Unable to process array with dimensionality " + nd);
			return;
		}
		
		// apply operator on current image
		Image resultImage = new Image(result, image);
		
		// choose appropriate suffix
		String suffix = opIndex == 0 ? "-extMin" : "-extMax";
		resultImage.setName(image.getName() + suffix);
		
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage, frame);
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
