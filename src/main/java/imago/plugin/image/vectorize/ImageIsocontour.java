/**
 * 
 */
package imago.plugin.image.vectorize;

import imago.app.ImageHandle;
import imago.app.shape.ImagoShape;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.Scalar;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;
import net.sci.image.vectorize.Isocontour;

/**
 * Compute boundary graph of a binary image.
 * 
 * @author David Legland
 *
 */
public class ImageIsocontour implements Plugin
{
	public ImageIsocontour()
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
		System.out.println("image isocontour");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
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
        ScalarArray2D<? extends Scalar> scalar = ScalarArray2D.wrap((ScalarArray<?>) array);
        

		// Choose the iso contour value
		GenericDialog dlg = new GenericDialog(frame, "Isocontour");
		double[] extent = scalar.finiteValueRange(); 
		dlg.addSlider("Isocontour Value", extent[0], extent[1], (extent[0] + extent[1]) / 2);
		dlg.showDialog();
		
		if (dlg.wasCanceled())
		    return;
		double value = dlg.getNextNumber();

		// create median box operator
		Geometry2D graph = new Isocontour(value).processScalar2d(scalar);

		// add to the document
        doc.addShape(new ImagoShape(graph));
                
        // TODO: maybe propagating events would be better
        ImageFrame viewer = (ImageFrame) frame;
        viewer.repaint(); 
	}

}
