/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;
import net.sci.image.discretize.Phantoms2D;

/**
 * Applies median box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class ImageFillDisk  implements FramePlugin
{
	public ImageFillDisk()
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
		Array<?> array = image.getData();

		if (!(array instanceof ScalarArray2D))
		{
		    throw new RuntimeException("Requires an image containing a ScalarArray2D");
		}
		
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
		GenericDialog gd = new GenericDialog(frame, "Fill Disk");
        gd.addNumericField("X Center ", sizeX / 2, 2);
        gd.addNumericField("Y Center ", sizeY / 2, 2);
        gd.addNumericField("Radius ", Math.min(sizeX, sizeY) / 4, 2);
        gd.addNumericField("Value ", 255, 2);
		
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
        double centerX = gd.getNextNumber();
        double centerY = gd.getNextNumber();
        double radius = gd.getNextNumber();
        double value = gd.getNextNumber();
		
		// create disk
        Phantoms2D.fillDisk((ScalarArray2D<?>) array, new Point2D(centerX, centerY), radius, value);
		
		// apply operator on current image
        ((ImageFrame) frame).getImageView().refreshDisplay();
		frame.repaint();
	}
}
