/**
 * 
 */
package imago.image.plugins.vectorize;

import imago.app.ImagoApp;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shapemanager.GeometryHandle;
import imago.shapemanager.ShapeManager;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
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
        dlg.addCheckBox("Add to Image shapes", true);
        dlg.addCheckBox("Add to Shape Manager", false);
        dlg.showDialog();

        if (dlg.wasCanceled()) return;
        double value = dlg.getNextNumber();
        boolean addToImage = dlg.getNextBoolean();
        boolean addToShapeManager = dlg.getNextBoolean();

        // create median box operator
        Geometry2D contour = new Isocontour(value).processScalar2d(scalar);

        // add to the document
        if(addToImage)
        {
            handle.addShape(new Shape(contour));
            handle.notifyImageHandleChange();
        }
        
        if (addToShapeManager)
        {
            ImagoApp app = frame.getGui().getAppli();
            GeometryHandle geomHandle = GeometryHandle.create(app, contour);
            
            // opens a dialog to choose name
            String name = geomHandle.getName();
            name = ImagoGui.showInputDialog(frame, "Name of new geometry:", "Choose Geometry Name", name);
            geomHandle.setName(name);
            
            // ensure ShapeManager is visible
            ShapeManager manager = ShapeManager.getInstance(frame.getGui());
            manager.repaint();
            manager.setVisible(true);
        }
	}
}
