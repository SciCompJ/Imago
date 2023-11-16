/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.geom.geom2d.Domain2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.geom.geom2d.curve.GenericDomain2D;
import net.sci.image.Image;
import net.sci.image.discretize.Phantoms2D;

/**
 * Applies median box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class ImageFillEllipse implements FramePlugin
{
	public ImageFillEllipse()
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
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        Image image = handle.getImage();
		Array<?> array = image.getData();

		if (!(array instanceof ScalarArray2D))
		{
		    throw new RuntimeException("Requires an image containing a ScalarArray2D");
		}
		
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        // create dialog to input ellipse parameters
		GenericDialog gd = new GenericDialog(frame, "Fill Disk");
        gd.addNumericField("X Center ", sizeX / 2, 2);
        gd.addNumericField("Y Center ", sizeY / 2, 2);
        gd.addNumericField("Radius 1 ", Math.min(sizeX, sizeY) / 4, 2);
        gd.addNumericField("Radius 2 ", Math.min(sizeX, sizeY) / 4, 2);
        gd.addNumericField("Orientation (degrees) ", 0, 2);
        gd.addNumericField("Value ", 255, 2);
		
		gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// extract ellipse parameters
        double centerX = gd.getNextNumber();
        double centerY = gd.getNextNumber();
        double r1 = gd.getNextNumber();
        double r2 = gd.getNextNumber();
        double thetaDeg = gd.getNextNumber();
        double value = gd.getNextNumber();
		
		// create disk
        Ellipse2D elli = new Ellipse2D(centerX, centerY, r1, r2, thetaDeg);
        Domain2D domain = new GenericDomain2D(elli);
        Phantoms2D.fillDomain((ScalarArray2D<?>) array, domain, value);
		
        // notify changes
        handle.notifyImageHandleChange(ImageHandle.Event.IMAGE_MASK | ImageHandle.Event.CHANGE_MASK);
	}
}
