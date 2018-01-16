/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.image.Image;

/**
 * Setup spatial calibraton of the image, assuming all dimensions are expressed
 * with the same unit.
 * 
 * @author David Legland
 *
 */
public class ImageSetScaleAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageSetScaleAction(ImagoFrame frame, String name)
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
		System.out.println("image set scale");

		// get current image data
		ImagoDocViewer viewer = (ImagoDocViewer) this.frame;
		ImagoDoc doc = viewer.getDocument();
		Image image	= doc.getImage();

		int nd = image.getDimension();
		
		
		GenericDialog gd = new GenericDialog(this.frame, "Set Image Scale");
		for (int d = 0; d < nd; d++)
		{
			gd.addNumericField("Pixel size [" + (d+1) + "]", 1.0, 6);
		}
		gd.addTextField("Unit Name:" , "");
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		double[] resolList = new double[nd];
		for (int d = 0; d < nd; d++)
		{
			resolList[d] = gd.getNextNumber();
		}
		String unitName = gd.getNextString();

		image.setSpatialCalibration(resolList, unitName);
		
		viewer.repaint();
	}

}
