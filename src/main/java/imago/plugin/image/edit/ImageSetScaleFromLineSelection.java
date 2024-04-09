/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.PlanarImageViewer;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.image.Calibration;
import net.sci.image.Image;

/**
 * Setup spatial calibration of the image, based on the current line selection
 * and a physical length given by user.
 * 
 * @author David Legland
 *
 */
public class ImageSetScaleFromLineSelection implements FramePlugin
{
	public ImageSetScaleFromLineSelection()
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
		ImageFrame imageFrame = (ImageFrame) frame;
		ImageHandle doc = imageFrame.getImageHandle();
		Image image	= doc.getImage();

		int nd = image.getDimension();
		
		// retrieve line selection
        PlanarImageViewer piv = (PlanarImageViewer) imageFrame.getImageViewer();
        Geometry selection = piv.getSelection();
        if (!(selection instanceof LineSegment2D))
        {
            System.out.println("requires selection to be a line segment");
            return;
        }
        
        LineSegment2D line = (LineSegment2D) selection;
		double lineLength = line.length();
		
		GenericDialog gd = new GenericDialog(frame, "Set Image Scale");
        gd.addNumericField("Selection length (pixels)", lineLength, 3);
        gd.addNumericField("Known length", lineLength, 3);
		gd.addTextField("Unit Name:" , "");
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// retrieve user inputs
		lineLength = gd.getNextNumber();
		double calibratedLength = gd.getNextNumber();
		String unitName = gd.getNextString();
		
		// compute resolution as size of pixel side
		double resol = calibratedLength / lineLength;
		
		// update Image calibration
		double[] resolList = new double[nd];
		for (int d = 0; d < nd; d++)
		{
			resolList[d] = resol;
		}
		Calibration calib = image.getCalibration();
		calib.setSpatialCalibration(resolList, unitName);
		
		// refresh display
		imageFrame.repaint();
	}

}
