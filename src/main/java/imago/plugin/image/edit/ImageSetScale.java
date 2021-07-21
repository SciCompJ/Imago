/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Calibration;
import net.sci.image.Image;

/**
 * Setup spatial calibration of the image, assuming all dimensions are expressed
 * with the same unit.
 * 
 * @author David Legland
 *
 */
public class ImageSetScale implements FramePlugin
{
	public ImageSetScale()
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
		System.out.println("image set scale");

		// get current image data
		ImageFrame viewer = (ImageFrame) frame;
		ImageHandle doc = viewer.getImageHandle();
		Image image	= doc.getImage();

		int nd = image.getDimension();
		
		
		GenericDialog gd = new GenericDialog(frame, "Set Image Scale");
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

		Calibration calib = image.getCalibration();
		calib.setSpatialCalibration(resolList, unitName);
		
		viewer.repaint();
	}

}
