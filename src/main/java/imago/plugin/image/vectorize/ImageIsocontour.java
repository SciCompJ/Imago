/**
 * 
 */
package imago.plugin.image.vectorize;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;
import net.sci.image.vectorize.Isocontour;

/**
 * Compute iso-value contours within a scalar image.
 * 
 * @author David Legland
 *
 */
public class ImageIsocontour implements FramePlugin
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
		// get current image data
		ImageHandle handle = ((ImageFrame) frame).getImageHandle();
		Image image	= handle.getImage();
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
			frame.showErrorDialog("Requires a scalar image input", "Data Type Error");
			return;
		}

		int nd = array.dimensionality();
		if (nd != 2)
		{
			frame.showErrorDialog("Can process only 2D", "Dimensionality Error");
			return;
		}

        // wrap array into a 2D scalar array
        ScalarArray2D<?> scalar = ScalarArray2D.wrap((ScalarArray<?>) array);

        // Choose the iso contour value
        GenericDialog dlg = new GenericDialog(frame, "Isocontour");
        double[] extent = scalar.finiteValueRange();
        dlg.addSlider("Isocontour Value", extent[0], extent[1], (extent[0] + extent[1]) / 2);
        dlg.showDialog();

        if (dlg.wasCanceled()) return;
        double value = dlg.getNextNumber();

        // create median box operator
        Geometry2D graph = new Isocontour(value).processScalar2d(scalar);

        // add to the document
        handle.addShape(new Shape(graph));
        handle.notifyImageHandleChange();
	}
}
