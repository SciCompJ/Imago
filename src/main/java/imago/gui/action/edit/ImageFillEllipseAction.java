/**
 * 
 */
package imago.gui.action.edit;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.scalar2d.ScalarArray2D;
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
public class ImageFillEllipseAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageFillEllipseAction(ImagoFrame frame, String name)
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
		System.out.println("fill ellipse");

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
        gd.addNumericField("Radius 1 ", Math.min(sizeX, sizeY) / 4, 2);
        gd.addNumericField("Radius 2 ", Math.min(sizeX, sizeY) / 4, 2);
        gd.addNumericField("Orientation (°) ", 0, 2);
        gd.addNumericField("Value ", 255, 2);
		
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
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
//        Phantoms2D.fillEllipse((ScalarArray2D<?>) array, elli, value);
//        Polygon2D poly = Polygon2D.create(elli.asPolyline(200).vertices());
//        Phantoms2D.fillPolygon((ScalarArray2D<?>) array, poly, value);
		
		// apply operator on current image
        ((ImagoDocViewer) this.frame).getImageView().refreshDisplay();
		frame.repaint();
	}
}
