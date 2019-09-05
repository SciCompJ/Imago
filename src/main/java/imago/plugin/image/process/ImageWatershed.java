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
import net.sci.array.scalar.IntArray;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;
import net.sci.image.data.Connectivity2D;
import net.sci.image.data.Connectivity3D;
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
public class ImageWatershed implements Plugin
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
		System.out.println("Watershed");

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
			ScalarArray2D<?> minima = MinimaAndMaxima.extendedMinima(array2d, dynamic, conn);
			ScalarArray2D<?> imposed = MinimaAndMaxima.imposeMinima(array2d, minima, conn);
			result = new Watershed2D(conn).process(imposed);
		}
		else if (nd == 3)
		{
		    ScalarArray3D<?> array3d = (ScalarArray3D<?>) ScalarArray3D.wrapScalar3d((ScalarArray<?>) array);
		    Connectivity3D conn = connIndex == 0 ? Connectivity3D.C6 : Connectivity3D.C26;
		    ScalarArray3D<?> minima = MinimaAndMaxima.extendedMinima(array3d, dynamic, conn);
		    ScalarArray3D<?> imposed = MinimaAndMaxima.imposeMinima(array3d, minima, conn);
		    result = new Watershed3D(conn).process(imposed);
		}
		else
		{
			System.err.println("Unable to process array with dimensionality " + nd);
			return;
		}
		
		// apply operator on current image
		Image resultImage = new Image(result, image);
		resultImage.setType(Image.Type.LABEL);
		
		// choose appropriate suffix
		String suffix = "-wat";
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

        return true;
    }
}
