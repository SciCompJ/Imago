/**
 * 
 */
package imago.gui.action.edit;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.data.scalar2d.ScalarArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;
import net.sci.image.discretize.Phantoms2D;

/**
 * Applies median box filtering on a multidimensional image.
 * 
 * @author David Legland
 *
 */
public class ImageFillDiskAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageFillDiskAction(ImagoFrame frame, String name)
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
	public void actionPerformed(ActionEvent arg0)
	{
		System.out.println("fill disk");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (!(array instanceof ScalarArray2D))
		{
		    throw new RuntimeException("Requires an image containing a ScalarArray2D");
		}
		
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        
		GenericDialog gd = new GenericDialog(this.frame, "Fill Disk");
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
        ((ImagoDocViewer) this.frame).getImageView().refreshDisplay();
		frame.repaint();
	}
}
