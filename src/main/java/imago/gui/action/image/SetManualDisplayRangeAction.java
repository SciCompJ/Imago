/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.image.Image;

import java.awt.event.ActionEvent;

/**
 * @author David Legland
 *
 */
public class SetManualDisplayRangeAction extends ImagoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SetManualDisplayRangeAction(ImagoFrame frame, String name) {
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.out.println("set manual display range");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		Array<?> array = image.getData();
//		if (image instanceof VectorImage) {
//			image = ((VectorImage) image).getNorm();
//		}
		
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar Image");
		}
		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		
		// Compute min and max values within the array 
		double[] extent = scalarArray.getValueRange();
		double[] displayRange = image.getDisplayRange();
		
		// Create new dialog populated with widgets
		GenericDialog gd = new GenericDialog(this.frame, "Set Display Range");
		String labelMin = "Min value (" + extent[0] + ")";
		gd.addNumericField(labelMin, displayRange[0], 3, "Minimal value to display as black");
		String labelMax = "Max value (" + extent[1] + ")";
		gd.addNumericField(labelMax, displayRange[1], 3, "Maximal value to display as white");
		
		// wait for user validation or cancellation
		gd.showDialog();
		if (gd.wasCanceled())
		{
			return;
		}
		
		// extract user inputs
		double minRange = gd.getNextNumber();
		double maxRange = gd.getNextNumber();
		extent = new double[]{minRange, maxRange};

		// update display settings
		image.setDisplayRange(extent);
		
		// refresh display
		ImageViewer viewer = ((ImagoDocViewer) this.frame).getImageView();
		viewer.refreshDisplay();
		viewer.repaint();
	}
}

