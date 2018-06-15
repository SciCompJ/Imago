/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.data.Connectivity2D;
import net.sci.image.data.Connectivity3D;
import net.sci.image.morphology.MinimaAndMaxima;

/**
 * Computes regional minima or maxima on a scalar image.
 * 
 * @author David Legland
 *
 */
public class ImageRegionalExtrema implements Plugin
{
	public ImageRegionalExtrema()
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
		System.out.println("Regional minima/maxima");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image	= doc.getImage();
		
		// current dimensionality
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar array");
		}
		int nd = array.dimensionality();
		
		// String lists for dialog widgets
		String[] operationNames = new String[]{"Regional Minima", "Regional Maxima"};
		String[] connectivityNames = new String[]{"Ortho", "Full"};

		// Creates generic dialog
		GenericDialog gd = new GenericDialog(frame, "Regional Min./Max.");
		gd.addChoice("Operation", operationNames, operationNames[0]);
		gd.addChoice("Connectivity: ", connectivityNames, connectivityNames[0]);
		gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		int opIndex = gd.getNextChoiceIndex();
		int connIndex = gd.getNextChoiceIndex();

		// Compute the result, depending on connectivity and extrema type
		Array<?> result;
		if (nd == 2)
		{
			Connectivity2D conn = connIndex == 0 ? Connectivity2D.C4 : Connectivity2D.C8;
			if (opIndex == 0)
				result = MinimaAndMaxima.regionalMinima((ScalarArray2D<?>) array, conn);
			else
				result = MinimaAndMaxima.regionalMaxima((ScalarArray2D<?>) array, conn);
		}
		else if (nd == 3)
        {
            Connectivity3D conn = connIndex == 0 ? Connectivity3D.C6 : Connectivity3D.C26;
            if (opIndex == 0)
                result = MinimaAndMaxima.regionalMinima((ScalarArray3D<?>) array, conn);
            else
                result = MinimaAndMaxima.regionalMinima((ScalarArray3D<?>) array, conn);
        }
		else
		{
			System.err.println("Unable to process array with dimensionality " + nd);
			return;
		}
		
		// apply operator on current image
		Image resultImage = new Image(result, image);
		
		// choose appropriate suffix
		String suffix = opIndex == 0 ? "-rMin" : "-rMax";
		resultImage.setName(image.getName() + suffix);
		
		// add the image document to GUI
		frame.getGui().addNewDocument(resultImage);
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
        if (!(frame instanceof ImagoDocViewer))
            return false;
        
        // check image
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.getData() instanceof ScalarArray;
    }
}
