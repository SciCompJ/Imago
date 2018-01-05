/**
 * 
 */
package imago.gui.action.process;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.array.data.scalar2d.ScalarArray2D;
import net.sci.array.type.Scalar;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;
import net.sci.image.vectorize.Isocontour;

/**
 * Compute boundary graph of a binary image.
 * 
 * @author David Legland
 *
 */
public class ImageIsocontourAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageIsocontourAction(ImagoFrame frame, String name)
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
		System.out.println("image isocontour");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray))
		{
			this.frame.showErrorDialog("Requires a scalar image input", "Data Type Error");
			return;
		}

		int nd = array.dimensionality();
		if (nd != 2)
		{
			this.frame.showErrorDialog("Can process only 2D", "Dimensionality Error");
			return;
		}

		// wrap array into a 2D scalar array
        ScalarArray2D<? extends Scalar> scalar = ScalarArray2D.wrap((ScalarArray<?>) array);
        

		// Choose the iso contour value
		GenericDialog dlg = new GenericDialog(this.frame, "Isocontour");
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
        ImagoDocViewer viewer = (ImagoDocViewer) this.frame;
        viewer.repaint(); 
	}

}
