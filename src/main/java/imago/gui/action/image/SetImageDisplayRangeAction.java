/**
 * 
 */
package imago.gui.action.image;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.data.ScalarArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class SetImageDisplayRangeAction extends ImagoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SetImageDisplayRangeAction(ImagoFrame frame, String name) {
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println("choose image display extent");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		Array<?> array = image.getData();
//		if (array instanceof VectorArray) {
//			array = ((VectorArray) array).getNorm();
//		}
		
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar Array");
		}
		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		
		// Compute min and max values within the array 
		double[] extent = scalarArray.getValueRange();
		System.out.println("Array value range: [" + extent[0] + " ; " + extent[1] + "]");
		
		image.setDisplayRange(extent);

		// refresh display
		ImageViewer viewer = ((ImagoDocViewer) this.frame).getImageView();
		viewer.refreshDisplay();
	}
}

